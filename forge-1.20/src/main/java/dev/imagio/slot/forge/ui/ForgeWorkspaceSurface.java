package dev.imagio.slot.forge.ui;

import dev.imagio.slot.forge.client.ForgeWorkspaceClient;
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
import dev.imagio.slot.ui.workspace.ShiftClickTransferState;
import dev.imagio.slot.ui.workspace.WallCardTransferGesturePolicy;
import dev.imagio.slot.ui.workspace.WallCardUiBuilder;
import dev.imagio.slot.ui.workspace.WallSectionHeaderUiBuilder;
import dev.imagio.slot.ui.workspace.WallSectionUiBuilder;
import dev.imagio.slot.ui.workspace.WayfindingDisplay;
import dev.imagio.slot.ui.workspace.WorkspaceGatherUiSupport;
import dev.imagio.slot.ui.workspace.WorkspaceSearchInputPolicy;
import dev.imagio.slot.ui.workspace.WorkspaceUiAttachments;
import dev.imagio.slot.ui.workspace.WorkspaceUiPalette;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Forge-local workspace controller shared by the full-screen surface and
 * vanilla-container sidebar. Common owns the widget builders and semantics;
 * this class owns Forge session, event, and packet plumbing.
 */
public final class ForgeWorkspaceSurface {
    public static final int WIDTH = 260;

    private static final int STANDALONE_BACKGROUND = 0x96060A0E;
    private static final int SIDEBAR_BACKGROUND = 0xD0060A0E;
    private static final int PANEL = 0xC8162029;
    private static final long REFRESH_INTERVAL_TICKS = 10L;
    private static final int RECENT_REHOME_CAPACITY = 6;
    private static final int RECENT_REHOME_MAX_DISPLAYED = 3;
    private static final int REHOME_MENU_MAX_DISPLAYED = 8;
    private static final int WHEEL_ACCUMULATOR_MAX_IDENTITIES = 64;

    private final Mode mode;
    private final WorkspaceActionEnvelope envelope;
    private final ForgeWorkspaceActionChannel actionChannel;
    private final List<SlotWorkspaceViewModel.AtlasIsland> islands = new ArrayList<>();
    private final List<SlotWorkspaceViewModel.AtlasItem> items = new ArrayList<>();
    private final List<SlotWorkspaceViewModel.IdentityRef> recents = new ArrayList<>();
    private final List<SlotWorkspaceViewModel.HotbarSlot> hotbarSlots = new ArrayList<>();
    private final Map<SlotWorkspaceViewModel.IdentityRef, SlotWorkspaceViewModel.AtlasItem> byIdentity =
            new LinkedHashMap<>();
    private final ShiftClickTransferState shiftClickTransferState = new ShiftClickTransferState();
    private final ArrayDeque<String> recentRehomeIslandIds = new ArrayDeque<>();
    private final Map<SlotWorkspaceViewModel.IdentityRef, Float> wheelAccumulatorByIdentity = new LinkedHashMap<>();

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
    private SlotWorkspaceViewModel.IdentityRef contextMenuIdentity;
    private String contextMenuKitId;
    private String contextMenuChestStorageId;
    private String renamingKitId;
    private String renameKitDraft = "";
    private String confirmDeleteKitId;
    private String renamingChestStorageId;
    private String renameChestDraft = "";
    private float contextMenuX;
    private float contextMenuY;
    private String editingIslandId;
    private String islandLabelDraft = "";
    private float islandEditX;
    private float islandEditY;
    private SlotWorkspaceViewModel.IdentityRef editingDesiredCountIdentity;
    private String desiredCountDraft = "";
    private SlotWorkspaceViewModel.IdentityRef pendingHomeDragIdentity;
    private String pendingHomeDragOriginIslandId;

    public ForgeWorkspaceSurface(Mode mode) {
        this.mode = mode == null ? Mode.STANDALONE : mode;
        this.envelope = new WorkspaceActionEnvelope(
                UUID.randomUUID().toString(),
                currentMenuContainerId(),
                0L);
        this.actionChannel = new ForgeWorkspaceActionChannel(envelope);
        this.status = this.mode == Mode.SIDEBAR
                ? "opening SLOT sidebar"
                : "opening SLOT workspace";
    }

    public void openSessionIfNeeded() {
        if (openSessionRequested) {
            return;
        }
        openSessionRequested = true;
        boolean sent = SlotForgeNetworking.openWorkspaceSession(new ForgeWorkspaceOpenMessage(envelope));
        status = sent ? openedStatus() : "failed to open SLOT workspace";
        if (sent) {
            lastRefreshGameTime = clientGameTime();
        }
        rebuildRequested = true;
    }

    public void tick(int width, int height) {
        shiftClickTransferState.observeShiftDown(Screen.hasShiftDown());
        openSessionIfNeeded();
        boolean pointerActive = tree != null && tree.hasActivePointerGesture();
        if (!pointerActive) {
            requestViewRefreshIfDue();
            applySyncedViewIfAvailable();
        }
        if (tree == null || (rebuildRequested && !pointerActive)) {
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
        pendingHomeDragIdentity = null;
        pendingHomeDragOriginIslandId = null;
        return tree.mouseClicked(mouseX, mouseY, button, Screen.hasShiftDown());
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (tree == null) {
            return false;
        }
        boolean hadActivePointerGesture = tree.hasActivePointerGesture();
        SlotWorkspaceViewModel.AtlasIsland releaseIsland = sectionAt(mouseX, mouseY);
        boolean handled = tree.mouseReleased(mouseX, mouseY, button, Screen.hasShiftDown());
        if (completePendingHomeDrag(releaseIsland)) {
            return true;
        }
        if (!hadActivePointerGesture && button == 0 && isCursorCarrying()
                && assignCursorHomeToSection(releaseIsland)) {
            return true;
        }
        pendingHomeDragIdentity = null;
        pendingHomeDragOriginIslandId = null;
        return handled;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (tree == null) {
            return false;
        }
        return tree.mouseScrolled(mouseX, mouseY, delta, 22f, Screen.hasShiftDown());
    }

    public boolean keyPressed(int keyCode, int scanCode) {
        return keyPressed(keyCode, scanCode, false);
    }

    public boolean keyPressed(int keyCode, int scanCode, boolean hostTextInputFocused) {
        if (hostTextInputFocused && !wantsKeyboardInput()) {
            return false;
        }
        if (handleEditorKey(keyCode)) {
            return true;
        }
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
        if (ForgeWorkspaceClient.matchesOpenVanilla(keyCode, scanCode)) {
            return openVanillaInventory();
        }
        if (ForgeWorkspaceClient.matchesCycleKitPage(keyCode, scanCode)) {
            int direction = Screen.hasShiftDown() ? -1 : 1;
            return switchKitPageFromKey(direction);
        }
        if (ForgeWorkspaceClient.matchesGatherActiveKit(keyCode, scanCode)) {
            return gatherActiveKitFromKey();
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

    private boolean openVanillaInventory() {
        if (mode == Mode.SIDEBAR) {
            return false;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.player == null) {
            status = "vanilla inventory unavailable";
            rebuildRequested = true;
            return true;
        }
        minecraft.setScreen(new InventoryScreen(minecraft.player));
        return true;
    }

    private boolean switchKitPageFromKey(int direction) {
        SlotWorkspaceViewModel.KitCard active = viewModel.activeKit();
        if (active == null || active.pageCount() <= 1) {
            status = active == null ? "activate a kit first" : "kit has one page";
            rebuildRequested = true;
            return true;
        }
        sendKitAction(WorkspaceActionId.SWITCH_KIT_PAGE, "switching kit page", direction);
        return true;
    }

    private boolean gatherActiveKitFromKey() {
        if (!anyGatherableIdentity()) {
            status = "nothing to gather";
            rebuildRequested = true;
            return true;
        }
        sendAction(WorkspaceActionId.GATHER_ACTIVE_KIT, "gathering desired items from nearby chests");
        return true;
    }

    public boolean charTyped(char codePoint) {
        return charTyped(codePoint, false);
    }

    public boolean charTyped(char codePoint, boolean hostTextInputFocused) {
        if (handleEditorChar(codePoint)) {
            return true;
        }
        return applySearchDecision(WorkspaceSearchInputPolicy.charTyped(
                searchActive,
                searchQuery,
                codePoint,
                hostTextInputFocused));
    }

    public boolean wantsKeyboardInput() {
        return searchActive
                || editingIslandId != null
                || editingDesiredCountIdentity != null
                || renamingKitId != null
                || renamingChestStorageId != null;
    }

    private String openedStatus() {
        if (mode == Mode.SIDEBAR) {
            return "opened chest sidebar";
        }
        return "opened SLOT workspace";
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
        status = displayStatus(synced.status(), synced.diagnostics());
        rebuildRequested = true;
    }

    private static String displayStatus(String nextStatus, String nextDiagnostics) {
        String base = nextStatus == null || nextStatus.isBlank() ? "ready" : nextStatus;
        if (nextDiagnostics == null || nextDiagnostics.isBlank()) {
            return base;
        }
        return base + " - " + nextDiagnostics;
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

        if (sidebarMode) {
            column.addChild(searchDepositRow(true));
            column.addChild(activeChestOrEmpty());
        } else {
            column.addChild(titleRow(false));
            column.addChild(searchDepositRow(false));
            SlotUiElement activeChestStrip = new ActiveChestStripUiBuilder(new ActiveChestContext())
                    .strip(viewModel.activeChestPanel());
            if (activeChestStrip != null) {
                column.addChild(activeChestStrip.layout(layout -> layout.width(WIDTH)));
            }
        }
        column.addChild(recents(sidebarMode));
        column.addChild(wallArea(sidebarMode));
        if (kitRackOpen) {
            column.addChild(kitRack(sidebarMode));
        }
        column.addChild(statusRow(sidebarMode));
        column.addChild(hotbar(sidebarMode));
        SlotUiElement overlay = activeOverlay();
        if (overlay != null) {
            column.addChild(overlay);
        }
        return column;
    }

    private SlotUiElement titleRow(boolean sidebarMode) {
        SlotUiElement row = SlotUiElement.element()
                .layout(layout -> {
                    if (sidebarMode) {
                        layout.widthPercent(100);
                    } else {
                        layout.width(WIDTH);
                    }
                    layout.height(16)
                            .gapAll(4)
                            .alignItems(SlotUiLayout.AlignItems.CENTER)
                            .flexDirection(SlotUiLayout.FlexDirection.ROW);
                });
        row.addChild(SlotUiElement.label("SLOT", WorkspaceUiPalette.ACCENT)
                .layout(layout -> layout.flex(1).heightPercent(100))
                .textStyle(style -> style
                        .color(WorkspaceUiPalette.ACCENT)
                        .fontSize(9)
                        .horizontal(SlotUiTextStyle.Horizontal.LEFT)
                        .vertical(SlotUiTextStyle.Vertical.CENTER)));
        if (!sidebarMode) {
            row.addChild(iconButton(
                            ForgeSlotUiTree.Icon.VANILLA_GRID,
                            true,
                            WorkspaceUiPalette.ROW_DIM,
                            WorkspaceUiPalette.TEXT,
                            "Open the vanilla inventory screen")
                    .on(SlotUiEventKind.CLICK, event -> {
                        if (event.button() != 0) {
                            return;
                        }
                        event.stopPropagation();
                        openVanillaInventory();
                    }));
        }
        return row;
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
        row.addChild(SlotUiElement.label(viewModel.carriedFreeSlotCount() + " free", WorkspaceUiPalette.TEXT)
                .layout(layout -> layout.width(44))
                .textStyle(style -> style
                        .color(WorkspaceUiPalette.TEXT)
                        .fontSize(7)
                        .horizontal(SlotUiTextStyle.Horizontal.CENTER)
                        .vertical(SlotUiTextStyle.Vertical.CENTER)));
        row.addChild(gatherButton());
        row.addChild(depositButton());
        return row;
    }

    private SlotUiElement gatherButton() {
        boolean enabled = anyGatherableIdentity();
        int color = enabled ? WorkspaceUiPalette.ROW_HOVER : WorkspaceUiPalette.ROW_DIM;
        return iconButton(
                        ForgeSlotUiTree.Icon.GATHER,
                        true,
                        color,
                        enabled ? WorkspaceUiPalette.TEXT : WorkspaceUiPalette.MUTED,
                        "Pull desired-count gaps and active-kit needs from nearby chests.")
                .on(SlotUiEventKind.CLICK, event -> {
                    if (event.button() != 0) {
                        return;
                    }
                    event.stopPropagation();
                    if (!enabled) {
                        status = "nothing to gather";
                        rebuildRequested = true;
                        return;
                    }
                    sendAction(WorkspaceActionId.GATHER_ACTIVE_KIT, "gathering desired items from nearby chests");
                });
    }

    private SlotUiElement depositButton() {
        return iconButton(
                        ForgeSlotUiTree.Icon.DEPOSIT,
                        true,
                        WorkspaceUiPalette.ROW_HOVER,
                        WorkspaceUiPalette.TEXT,
                        "Deposit carried items into nearby chests by learned affinity or matching contents.")
                .tooltip(Component.literal(
                        "Deposit carried items into nearby chests by learned affinity or matching contents. "
                                + "Items without either signal stay in carry."))
                .on(SlotUiEventKind.CLICK, event -> {
                    if (event.button() != 0) {
                        return;
                    }
                    event.stopPropagation();
                    sendAction(WorkspaceActionId.DEPOSIT, "depositing carried items");
                });
    }

    private SlotUiElement iconButton(
            ForgeSlotUiTree.Icon icon,
            boolean enabled,
            int color,
            int iconColor,
            String tooltip
    ) {
        return SlotUiElement.button("", enabled, color)
                .noText()
                .attach(ForgeSlotUiTree.ICON, icon)
                .tooltip(Component.literal(tooltip == null ? "" : tooltip))
                .layout(layout -> layout.width(16).height(16))
                .textStyle(style -> style
                        .color(iconColor)
                        .horizontal(SlotUiTextStyle.Horizontal.CENTER)
                        .vertical(SlotUiTextStyle.Vertical.CENTER));
    }

    private SlotUiElement kitRack(boolean sidebarMode) {
        return SlotUiElement.element()
                .layout(layout -> {
                    if (sidebarMode) {
                        layout.widthPercent(100);
                    } else {
                        layout.width(WIDTH);
                    }
                    layout.flexDirection(SlotUiLayout.FlexDirection.COLUMN);
                })
                .addChild(new KitRackUiBuilder(new KitContext()).rack(viewModel));
    }

    private SlotUiElement recents(boolean sidebarMode) {
        SlotUiElement strip = new RecentsStripUiBuilder(new RecentsContext()).overlay(recents);
        if (sidebarMode) {
            return strip;
        }
        return SlotUiElement.element()
                .layout(layout -> layout.width(WIDTH))
                .addChild(strip);
    }

    private SlotUiElement wallArea(boolean sidebarMode) {
        SlotUiElement area = SlotUiElement.element()
                .layout(layout -> {
                    if (sidebarMode) {
                        layout.widthPercent(100);
                    } else {
                        layout.width(WIDTH);
                    }
                    layout.flex(1)
                            .flexDirection(SlotUiLayout.FlexDirection.COLUMN);
                });
        SlotUiElement row = SlotUiElement.element()
                .layout(layout -> layout
                        .widthPercent(100)
                        .heightPercent(100)
                        .gapAll(3)
                        .alignItems(SlotUiLayout.AlignItems.STRETCH)
                        .flexDirection(SlotUiLayout.FlexDirection.ROW));
        row.addChild(tocStrip());
        row.addChild(SlotUiElement.element()
                .layout(layout -> layout
                        .flex(1)
                        .heightPercent(100)
                        .flexDirection(SlotUiLayout.FlexDirection.COLUMN))
                .addChild(wallViewport(sidebarMode)));
        area.addChild(row);
        return area;
    }

    private SlotUiElement tocStrip() {
        List<SlotWorkspaceViewModel.AtlasIsland> entries = tocEntries();
        SlotUiElement strip = SlotUiElement.panel(0x7010171D)
                .tooltip(Component.literal("Section index"))
                .layout(layout -> layout
                        .width(7)
                        .heightPercent(100)
                        .paddingVertical(3)
                        .gapAll(2)
                        .alignItems(SlotUiLayout.AlignItems.CENTER)
                        .flexDirection(SlotUiLayout.FlexDirection.COLUMN));
        if (entries.isEmpty()) {
            strip.allowHitTest(false);
            return strip;
        }
        int count = entries.size();
        for (int index = 0; index < count; index++) {
            SlotWorkspaceViewModel.AtlasIsland island = entries.get(index);
            float fraction = count <= 1 ? 0f : (float) index / (float) (count - 1);
            int color = island.color() == 0 ? WorkspaceUiPalette.MUTED : island.color();
            SlotUiElement dot = SlotUiElement.button("", true, color)
                    .noText()
                    .tooltip(Component.literal(island.label()))
                    .layout(layout -> layout.width(5).height(5));
            dot.on(SlotUiEventKind.TICK, event -> dotAttention(dot, island));
            dot.on(SlotUiEventKind.CLICK, event -> {
                if (event.button() != 0) {
                    return;
                }
                event.stopPropagation();
                if (tree != null) {
                    if (!tree.scrollToElementId(island.islandId())) {
                        tree.scrollToFraction(fraction);
                    }
                }
                setStatus(island.label());
            });
            strip.addChild(dot);
        }
        return strip;
    }

    private List<SlotWorkspaceViewModel.AtlasIsland> tocEntries() {
        ArrayList<SlotWorkspaceViewModel.AtlasIsland> entries = new ArrayList<>();
        boolean filtering = !normalizedSearchQuery().isBlank();
        for (SlotWorkspaceViewModel.AtlasIsland island : islands) {
            if (island == null || island.kind() != VisualAtlasIslandKind.PLAYER) {
                continue;
            }
            boolean hasVisibleItems = false;
            for (SlotWorkspaceViewModel.AtlasItem item : items) {
                if (item == null || !island.islandId().equals(item.islandId())) {
                    continue;
                }
                if (!filtering || matchesSearch(item)) {
                    hasVisibleItems = true;
                    break;
                }
            }
            if (hasVisibleItems) {
                entries.add(island);
            }
        }
        return entries;
    }

    private void dotAttention(SlotUiElement dot, SlotWorkspaceViewModel.AtlasIsland island) {
        if (dot == null || island == null) {
            return;
        }
        dot.overlayColor(sectionNeedsAttention(island) ? 0x66365743 : null);
    }

    private boolean sectionNeedsAttention(SlotWorkspaceViewModel.AtlasIsland island) {
        if (tree == null || island == null || !tree.isElementOffscreen(island.islandId())) {
            return false;
        }
        boolean searching = !normalizedSearchQuery().isBlank();
        for (SlotWorkspaceViewModel.AtlasItem item : items) {
            if (item == null || !island.islandId().equals(item.islandId())) {
                continue;
            }
            if (hoveredIdentity != null && hoveredIdentity.equals(item.identity())) {
                return true;
            }
            if (searching && matchesSearch(item)) {
                return true;
            }
            if (item.kitNeeded()) {
                return true;
            }
        }
        return false;
    }

    private SlotUiElement wallViewport(boolean sidebarMode) {
        SlotUiElement viewport = SlotUiElement.panel(0xB810171D)
                .attach(ForgeSlotUiTree.SCROLL_VIEWPORT, Boolean.TRUE)
                .layout(layout -> {
                    layout.widthPercent(100)
                            .flex(1)
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
        SlotUiElement kit = new KitRackUiBuilder(new KitContext())
                .cluster(viewModel, kitRackOpen, true)
                .layout(layout -> layout.height(KitRackUiBuilder.CLUSTER_HEIGHT_PX));
        SlotUiElement belt = new HotbarBeltUiBuilder(new HotbarContext()).belt(hotbarSlots, offhand, kit);
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
                        layout.widthPercent(100).height(12).paddingHorizontal(4);
                    } else {
                        layout.width(WIDTH).height(12).paddingHorizontal(4);
                    }
                    layout.alignItems(SlotUiLayout.AlignItems.CENTER)
                            .flexDirection(SlotUiLayout.FlexDirection.ROW);
                })
                .addChild(SlotUiElement.label(status, WorkspaceUiPalette.MUTED)
                        .layout(layout -> layout.flex(1).heightPercent(100))
                        .textStyle(style -> style
                                .color(WorkspaceUiPalette.MUTED)
                                .fontSize(6)
                                .horizontal(SlotUiTextStyle.Horizontal.LEFT)
                                .vertical(SlotUiTextStyle.Vertical.CENTER)));
    }

    private SlotUiElement activeOverlay() {
        if (editingDesiredCountIdentity != null) {
            return desiredCountOverlay();
        }
        if (editingIslandId != null) {
            return islandEditOverlay();
        }
        if (contextMenuKitId != null) {
            return kitContextOverlay();
        }
        if (contextMenuChestStorageId != null) {
            return chestContextOverlay();
        }
        if (contextMenuIdentity != null) {
            return itemContextOverlay();
        }
        return null;
    }

    private SlotUiElement overlayRoot() {
        return SlotUiElement.panel(0x33000000)
                .zIndex(500)
                .layout(layout -> layout
                        .positionType(SlotUiLayout.PositionType.ABSOLUTE)
                        .left(0)
                        .top(0)
                        .widthPercent(100)
                        .heightPercent(100))
                .on(SlotUiEventKind.MOUSE_DOWN, event -> {
                    event.stopPropagation();
                    closeOverlays();
                });
    }

    private SlotUiElement overlayPanel(float screenX, float screenY, float width) {
        float x = mode == Mode.SIDEBAR
                ? Math.max(4f, Math.min(screenX, WIDTH - width - 4f))
                : Math.max(4f, screenX);
        float y = Math.max(8f, screenY - 4f);
        return SlotUiElement.panel(0xF00B1117)
                .zIndex(501)
                .layout(layout -> layout
                        .positionType(SlotUiLayout.PositionType.ABSOLUTE)
                        .left(x)
                        .top(y)
                        .width(width)
                        .paddingAll(6)
                        .gapAll(4)
                        .flexDirection(SlotUiLayout.FlexDirection.COLUMN))
                .on(SlotUiEventKind.MOUSE_DOWN, event -> event.stopPropagation(), true);
    }

    private SlotUiElement itemContextOverlay() {
        SlotWorkspaceViewModel.AtlasItem item = byIdentity.get(contextMenuIdentity);
        if (item == null) {
            contextMenuIdentity = null;
            return null;
        }
        SlotUiElement overlay = overlayRoot();
        SlotUiElement panel = overlayPanel(contextMenuX, contextMenuY, 174);
        panel.addChild(menuLabel(shorten(item.name(), 30), WorkspaceUiPalette.TEXT));
        if (item.ghost()) {
            panel.addChild(menuButton(
                    "Take stack",
                    item.proximateCount() > 0,
                    "Take this stack from a nearby chest",
                    closeThen(() -> sendIdentityRefAction(
                            WorkspaceActionId.TAKE_STACK_BY_IDENTITY,
                            item.identity(),
                            "taking " + item.name()))));
        }
        panel.addChild(menuButton(
                "Put in hotbar",
                item.carried(),
                "Move this carried item to a free hotbar slot",
                closeThen(() -> sendIdentityRefAction(
                        WorkspaceActionId.ASSIGN_HOME_TO_HOTBAR_ONLY,
                        item.identity(),
                        "moving to hotbar"))));
        panel.addChild(menuButton(
                "Deposit to chest",
                item.carried() && anyChestProximate(),
                "Deposit this item into a nearby chest",
                closeThen(() -> sendIdentityRefAction(
                        WorkspaceActionId.DEPOSIT_HOME_TO_LINKED_CHEST,
                        item.identity(),
                        "depositing " + item.name()))));
        panel.addChild(menuButton(
                item.desiredCount() > 0 ? "Desired: " + item.desiredCount() : "Set desired count",
                true,
                "Set the player-global desired count for this item",
                () -> beginDesiredCountEdit(item)));
        if (item.desiredCount() > 0 && !item.desiredCountFromKit()) {
            panel.addChild(menuButton(
                    "Clear desired",
                    true,
                    "Clear the player-global desired count",
                    closeThen(() -> sendIdentityRefAction(
                            WorkspaceActionId.SET_PLAYER_DESIRED_COUNT,
                            item.identity(),
                            "clearing desired count",
                            0))));
        }
        panel.addChild(menuButton(
                "New section",
                true,
                "Create a new section for this item",
                closeThen(() -> sendIdentityRefAction(
                        WorkspaceActionId.CREATE_NAMED_ISLAND,
                        item.identity(),
                        "creating section",
                        item.name(),
                        WorkspaceUiPalette.ISLAND_SWATCHES[
                                Math.floorMod(islands.size(), WorkspaceUiPalette.ISLAND_SWATCHES.length)],
                        0,
                        0))));
        panel.addChild(menuLabel("Move home", WorkspaceUiPalette.MUTED));
        int targetCount = 0;
        for (SlotWorkspaceViewModel.AtlasIsland island : rehomeMenuTargets(item)) {
            targetCount++;
            panel.addChild(menuButton(
                    shorten(island.label(), 26),
                    true,
                    "Move this item's home to " + island.label(),
                    closeThen(() -> sendAssignHome(item.identity(), island.islandId(), null, "moving home"))));
        }
        if (targetCount == 0) {
            panel.addChild(menuLabel("No other sections", WorkspaceUiPalette.MUTED));
        }
        overlay.addChild(panel);
        return overlay;
    }

    private List<SlotWorkspaceViewModel.AtlasIsland> rehomeMenuTargets(SlotWorkspaceViewModel.AtlasItem item) {
        ArrayList<SlotWorkspaceViewModel.AtlasIsland> result = new ArrayList<>();
        String currentIslandId = item == null ? "" : item.islandId();
        for (String islandId : recentRehomeIslandIds) {
            if (islandId.equals(currentIslandId)) {
                continue;
            }
            SlotWorkspaceViewModel.AtlasIsland island = island(islandId);
            if (island == null || island.kind() != VisualAtlasIslandKind.PLAYER) {
                continue;
            }
            result.add(island);
            if (result.size() >= RECENT_REHOME_MAX_DISPLAYED) {
                break;
            }
        }
        for (SlotWorkspaceViewModel.AtlasIsland island : islands) {
            if (island == null
                    || island.kind() != VisualAtlasIslandKind.PLAYER
                    || island.islandId().equals(currentIslandId)
                    || containsIsland(result, island.islandId())) {
                continue;
            }
            result.add(island);
            if (result.size() >= REHOME_MENU_MAX_DISPLAYED) {
                break;
            }
        }
        return List.copyOf(result);
    }

    private static boolean containsIsland(List<SlotWorkspaceViewModel.AtlasIsland> islands, String islandId) {
        if (islands == null || islandId == null) {
            return false;
        }
        for (SlotWorkspaceViewModel.AtlasIsland island : islands) {
            if (island != null && islandId.equals(island.islandId())) {
                return true;
            }
        }
        return false;
    }

    private SlotUiElement kitContextOverlay() {
        SlotWorkspaceViewModel.KitCard kit = viewModel.kit(contextMenuKitId);
        if (kit == null) {
            closeOverlayState();
            return null;
        }
        SlotUiElement overlay = overlayRoot();
        SlotUiElement panel = overlayPanel(contextMenuX, contextMenuY, 178);
        panel.addChild(menuLabel(shorten(kit.name(), 30), WorkspaceUiPalette.ACCENT));
        if (kit.kitId().equals(renamingKitId)) {
            panel.addChild(menuLabel("Name: " + renameKitDraft + "_", WorkspaceUiPalette.TEXT));
            panel.addChild(menuButton("Save", true, "Rename this kit", this::commitKitRenameEdit));
            panel.addChild(menuButton("Cancel", true, "Close", this::closeOverlays));
        } else if (kit.kitId().equals(confirmDeleteKitId)) {
            panel.addChild(menuLabel("Delete this kit?", WorkspaceUiPalette.MUTED));
            panel.addChild(menuButton(
                    "Delete",
                    true,
                    "Delete this kit",
                    closeThen(() -> sendKitAction(WorkspaceActionId.DELETE_KIT, "deleting kit", kit.kitId()))));
            panel.addChild(menuButton("Cancel", true, "Close", () -> {
                confirmDeleteKitId = null;
                rebuildRequested = true;
            }));
        } else {
            panel.addChild(menuButton("Rename...", true, "Rename this kit", () -> beginKitRenameEdit(kit)));
            panel.addChild(menuButton(
                    "Duplicate",
                    true,
                    "Duplicate this kit",
                    closeThen(() -> sendKitAction(WorkspaceActionId.DUPLICATE_KIT, "duplicating kit", kit.kitId()))));
            panel.addChild(menuButton("Delete...", true, "Delete this kit", () -> {
                confirmDeleteKitId = kit.kitId();
                renamingKitId = null;
                renameKitDraft = "";
                rebuildRequested = true;
            }));
            panel.addChild(menuButton("Cancel", true, "Close", this::closeOverlays));
        }
        overlay.addChild(panel);
        return overlay;
    }

    private SlotUiElement chestContextOverlay() {
        SlotWorkspaceViewModel.ChestChip chip = viewModel.chestChip(contextMenuChestStorageId);
        if (chip == null) {
            closeOverlayState();
            return null;
        }
        SlotUiElement overlay = overlayRoot();
        SlotUiElement panel = overlayPanel(contextMenuX, contextMenuY, 178);
        String label = chip.label().isBlank() ? "Chest" : chip.label();
        panel.addChild(menuLabel(shorten(label, 30), WorkspaceUiPalette.ACCENT));
        if (chip.storageId().equals(renamingChestStorageId)) {
            panel.addChild(menuLabel("Name: " + renameChestDraft + "_", WorkspaceUiPalette.TEXT));
            panel.addChild(menuButton("Save", true, "Rename this chest", this::commitChestRenameEdit));
            panel.addChild(menuButton("Cancel", true, "Close", this::closeOverlays));
        } else {
            panel.addChild(menuButton("Rename...", true, "Rename this chest", () -> beginChestRenameEdit(chip)));
            panel.addChild(menuButton(
                    "Forget chest",
                    true,
                    "Forget this claimed chest",
                    closeThen(() -> sendAction(WorkspaceActionId.FORGET_CHEST, "forgetting chest", chip.storageId()))));
            panel.addChild(menuButton("Cancel", true, "Close", this::closeOverlays));
        }
        overlay.addChild(panel);
        return overlay;
    }

    private SlotUiElement desiredCountOverlay() {
        SlotWorkspaceViewModel.IdentityRef identity = editingDesiredCountIdentity;
        SlotWorkspaceViewModel.AtlasItem item = byIdentity.get(identity);
        SlotUiElement overlay = overlayRoot();
        SlotUiElement panel = overlayPanel(contextMenuX, contextMenuY, 170);
        panel.addChild(menuLabel("Desired count", WorkspaceUiPalette.ACCENT));
        panel.addChild(menuLabel(shorten(item == null ? identity.itemId() : item.name(), 30), WorkspaceUiPalette.TEXT));
        panel.addChild(menuLabel("Count: " + (desiredCountDraft.isBlank() ? "0" : desiredCountDraft) + "_",
                WorkspaceUiPalette.TEXT));
        panel.addChild(menuButton("Save", true, "Save desired count", this::commitDesiredCountEdit));
        panel.addChild(menuButton("Clear", true, "Clear desired count", () -> {
            desiredCountDraft = "0";
            commitDesiredCountEdit();
        }));
        panel.addChild(menuButton("Cancel", true, "Close", this::closeOverlays));
        overlay.addChild(panel);
        return overlay;
    }

    private SlotUiElement islandEditOverlay() {
        SlotWorkspaceViewModel.AtlasIsland island = island(editingIslandId);
        if (island == null) {
            editingIslandId = null;
            return null;
        }
        SlotUiElement overlay = overlayRoot();
        SlotUiElement panel = overlayPanel(islandEditX, islandEditY, 178);
        panel.addChild(menuLabel("Edit section", WorkspaceUiPalette.ACCENT));
        panel.addChild(menuLabel("Name: " + islandLabelDraft + "_", WorkspaceUiPalette.TEXT));
        panel.addChild(menuButton("Save name", true, "Rename this section", this::commitIslandNameEdit));

        SlotUiElement swatches = SlotUiElement.element()
                .layout(layout -> layout
                        .widthPercent(100)
                        .height(14)
                        .gapAll(3)
                        .flexDirection(SlotUiLayout.FlexDirection.ROW));
        for (int color : WorkspaceUiPalette.ISLAND_SWATCHES) {
            swatches.addChild(SlotUiElement.button("", true, color)
                    .noText()
                    .tooltip(Component.literal("Set section color"))
                    .layout(layout -> layout.flex(1).height(12))
                    .on(SlotUiEventKind.CLICK, event -> {
                        if (event.button() != 0) {
                            return;
                        }
                        event.stopPropagation();
                        closeOverlayState();
                        sendAction(WorkspaceActionId.RECOLOR_ISLAND, "recoloring section", island.islandId(), color);
                    }));
        }
        panel.addChild(swatches);

        boolean hasHoveredItem = hoveredIdentity != null && byIdentity.containsKey(hoveredIdentity);
        panel.addChild(menuButton(
                "Use hovered icon",
                hasHoveredItem,
                "Set the section icon to the currently hovered item",
                closeThen(() -> {
                    SlotWorkspaceViewModel.IdentityRef icon = hoveredIdentity;
                    sendAction(
                            WorkspaceActionId.SET_ISLAND_ICON,
                            "setting section icon",
                            island.islandId(),
                            icon.itemId(),
                            icon.comparisonMode(),
                            icon.componentFingerprint());
                })));
        panel.addChild(menuButton(
                "Clear icon",
                true,
                "Clear the section icon",
                closeThen(() -> sendAction(WorkspaceActionId.SET_ISLAND_ICON, "clearing section icon",
                        island.islandId(), "", "", ""))));
        panel.addChild(menuButton(
                "Delete section",
                island.itemCount() == 0,
                island.itemCount() == 0 ? "Delete this empty section" : "Move items out before deleting",
                closeThen(() -> sendAction(WorkspaceActionId.DELETE_ISLAND, "deleting section", island.islandId()))));
        panel.addChild(menuButton("Close", true, "Close", this::closeOverlays));
        overlay.addChild(panel);
        return overlay;
    }

    private SlotUiElement menuLabel(String text, int color) {
        return SlotUiElement.label(text, color)
                .layout(layout -> layout.widthPercent(100).height(11))
                .textStyle(style -> style
                        .color(color)
                        .fontSize(7)
                        .horizontal(SlotUiTextStyle.Horizontal.LEFT)
                        .vertical(SlotUiTextStyle.Vertical.CENTER));
    }

    private SlotUiElement menuButton(String text, boolean enabled, String inactiveStatus, Runnable action) {
        return SlotUiElement.button(text, enabled, enabled ? WorkspaceUiPalette.ROW_HOVER : WorkspaceUiPalette.ROW_DIM)
                .tooltip(Component.literal(inactiveStatus == null ? "" : inactiveStatus))
                .layout(layout -> layout.widthPercent(100).height(14))
                .textStyle(style -> style
                        .color(enabled ? WorkspaceUiPalette.TEXT : WorkspaceUiPalette.MUTED)
                        .fontSize(7)
                        .horizontal(SlotUiTextStyle.Horizontal.CENTER)
                        .vertical(SlotUiTextStyle.Vertical.CENTER))
                .on(SlotUiEventKind.CLICK, event -> {
                    if (event.button() != 0) {
                        return;
                    }
                    event.stopPropagation();
                    if (!enabled) {
                        setStatus(inactiveStatus);
                        return;
                    }
                    if (action != null) {
                        action.run();
                    }
                });
    }

    private Runnable closeThen(Runnable action) {
        return () -> {
            closeOverlayState();
            if (action != null) {
                action.run();
            }
        };
    }

    private SlotUiElement enrichSection(
            SlotUiElement section,
            List<SlotWorkspaceViewModel.AtlasItem> islandItems
    ) {
        SlotUiElement header = null;
        SlotUiElement grid = null;
        SlotWorkspaceViewModel.AtlasIsland island = null;
        for (SlotUiElement child : section.children()) {
            if (child.hasAttachment(WorkspaceUiAttachments.WALL_SECTION_HEADER)) {
                header = child;
                island = child.attachment(WorkspaceUiAttachments.ATLAS_ISLAND, SlotWorkspaceViewModel.AtlasIsland.class);
            }
            if (child.hasAttachment(WorkspaceUiAttachments.WALL_SECTION_GRID)) {
                grid = child;
                if (island == null) {
                    island = child.attachment(
                            WorkspaceUiAttachments.ATLAS_ISLAND,
                            SlotWorkspaceViewModel.AtlasIsland.class);
                }
            }
        }
        installSectionHomeTarget(header, island);
        installSectionHomeTarget(grid, island);
        if (grid == null) {
            return section;
        }
        WallCardUiBuilder cardBuilder = new WallCardUiBuilder(new CardContext());
        for (SlotWorkspaceViewModel.AtlasItem item : islandItems) {
            SlotUiElement card = cardBuilder.card(item);
            card.on(SlotUiEventKind.MOUSE_DOWN, event -> {
                if (event.button() == 0) {
                    beginHomeDragCandidate(freshItem(item));
                    event.stopPropagation();
                    return;
                }
                if (event.button() == 1 && !isCursorCarrying()) {
                    event.stopPropagation();
                    openItemContextMenu(freshItem(item), event.x(), event.y());
                    return;
                }
                SlotWorkspaceViewModel.AtlasItem target = freshItem(item);
                WallCardTransferGesturePolicy.Decision decision = WallCardTransferGesturePolicy.pointerDown(
                        cardGestureContext(target, event.button(), event.shiftDown()));
                if (dispatchCardGestureDecision(target, decision)) {
                    event.stopPropagation();
                }
            });
            card.on(SlotUiEventKind.CLICK, event -> {
                if (event.button() != 0) {
                    return;
                }
                event.stopPropagation();
                SlotWorkspaceViewModel.AtlasItem target = freshItem(item);
                WallCardTransferGesturePolicy.Decision decision = WallCardTransferGesturePolicy.click(
                        cardGestureContext(target, event.button(), event.shiftDown()));
                dispatchCardGestureDecision(target, decision);
            });
            card.on(SlotUiEventKind.MOUSE_WHEEL, event -> {
                boolean controlDown = Screen.hasControlDown();
                if (!event.shiftDown() && !controlDown) {
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
                SlotWorkspaceViewModel.AtlasItem target = freshItem(item);
                int steps = wheelSteps(target == null ? null : target.identity(), delta, controlDown);
                if (steps == 0) {
                    return;
                }
                WallCardTransferGesturePolicy.Decision decision = WallCardTransferGesturePolicy.wheel(
                        cardGestureContext(target, 0, event.shiftDown(), controlDown),
                        steps);
                dispatchCardGestureDecision(target, decision);
            });
            grid.addChild(card);
        }
        return section;
    }

    private void installSectionHomeTarget(
            SlotUiElement target,
            SlotWorkspaceViewModel.AtlasIsland island
    ) {
        if (target == null || island == null || island.kind() == VisualAtlasIslandKind.TRIAGE) {
            return;
        }
        target.on(SlotUiEventKind.MOUSE_DOWN, event -> {
            if (event.propagationStopped() || event.button() != 0 || !isCursorCarrying()) {
                return;
            }
            event.stopPropagation();
            assignCursorHomeToSection(island);
        });
    }

    private SlotWorkspaceViewModel.AtlasItem freshItem(SlotWorkspaceViewModel.AtlasItem item) {
        if (item == null || viewModel == null) {
            return item;
        }
        SlotWorkspaceViewModel.AtlasItem fresh = viewModel.atlasItem(item.identity());
        return fresh == null ? item : fresh;
    }

    private void beginHomeDragCandidate(SlotWorkspaceViewModel.AtlasItem item) {
        if (item == null || item.identity() == null || isCursorCarrying()) {
            pendingHomeDragIdentity = null;
            pendingHomeDragOriginIslandId = null;
            return;
        }
        pendingHomeDragIdentity = item.identity();
        pendingHomeDragOriginIslandId = item.islandId();
    }

    private boolean completePendingHomeDrag(SlotWorkspaceViewModel.AtlasIsland targetIsland) {
        SlotWorkspaceViewModel.IdentityRef identity = pendingHomeDragIdentity;
        String originIslandId = pendingHomeDragOriginIslandId;
        pendingHomeDragIdentity = null;
        pendingHomeDragOriginIslandId = null;
        if (identity == null || targetIsland == null || targetIsland.kind() == VisualAtlasIslandKind.TRIAGE) {
            return false;
        }
        if (targetIsland.islandId().equals(originIslandId)) {
            return false;
        }
        return sendAssignHome(identity, targetIsland.islandId(), null, "moving home");
    }

    private boolean assignCursorHomeToSection(SlotWorkspaceViewModel.AtlasIsland island) {
        SlotWorkspaceViewModel.IdentityRef identity = cursorIdentity();
        if (identity == null || island == null || island.kind() == VisualAtlasIslandKind.TRIAGE) {
            return false;
        }
        return sendAssignHome(identity, island.islandId(), null, "moving cursor home");
    }

    private SlotWorkspaceViewModel.AtlasIsland sectionAt(double mouseX, double mouseY) {
        return tree == null
                ? null
                : tree.attachmentAt(
                        mouseX,
                        mouseY,
                        WorkspaceUiAttachments.ATLAS_ISLAND,
                        SlotWorkspaceViewModel.AtlasIsland.class);
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

    private boolean anyGatherableIdentity() {
        for (SlotWorkspaceViewModel.AtlasItem item : items) {
            if (WorkspaceGatherUiSupport.isGatherableItem(item)) {
                return true;
            }
        }
        return false;
    }

    private SlotWorkspaceViewModel.AtlasIsland island(String islandId) {
        for (SlotWorkspaceViewModel.AtlasIsland island : islands) {
            if (island.islandId().equals(islandId)) {
                return island;
            }
        }
        return null;
    }

    private void openItemContextMenu(SlotWorkspaceViewModel.AtlasItem item, float screenX, float screenY) {
        if (item == null || item.identity() == null) {
            setStatus("missing item identity");
            return;
        }
        contextMenuIdentity = item.identity();
        contextMenuKitId = null;
        contextMenuChestStorageId = null;
        contextMenuX = screenX;
        contextMenuY = screenY;
        editingIslandId = null;
        editingDesiredCountIdentity = null;
        renamingKitId = null;
        renameKitDraft = "";
        confirmDeleteKitId = null;
        renamingChestStorageId = null;
        renameChestDraft = "";
        status = item.name();
        rebuildRequested = true;
    }

    private void openKitContextMenu(String kitId, float screenX, float screenY) {
        if (kitId == null || kitId.isBlank()) {
            setStatus("missing kit");
            return;
        }
        contextMenuKitId = kitId;
        contextMenuIdentity = null;
        contextMenuChestStorageId = null;
        contextMenuX = screenX;
        contextMenuY = screenY;
        editingIslandId = null;
        editingDesiredCountIdentity = null;
        renamingKitId = null;
        renameKitDraft = "";
        confirmDeleteKitId = null;
        renamingChestStorageId = null;
        renameChestDraft = "";
        status = "kit menu";
        rebuildRequested = true;
    }

    private void openChestContextMenu(String storageId, float screenX, float screenY) {
        if (storageId == null || storageId.isBlank()) {
            setStatus("missing chest");
            return;
        }
        contextMenuChestStorageId = storageId;
        contextMenuIdentity = null;
        contextMenuKitId = null;
        contextMenuX = screenX;
        contextMenuY = screenY;
        editingIslandId = null;
        editingDesiredCountIdentity = null;
        renamingKitId = null;
        renameKitDraft = "";
        confirmDeleteKitId = null;
        renamingChestStorageId = null;
        renameChestDraft = "";
        status = "chest menu";
        rebuildRequested = true;
    }

    private void beginDesiredCountEdit(SlotWorkspaceViewModel.AtlasItem item) {
        if (item == null || item.identity() == null) {
            setStatus("missing item identity");
            return;
        }
        editingDesiredCountIdentity = item.identity();
        desiredCountDraft = item.desiredCount() > 0 && !item.desiredCountFromKit()
                ? Integer.toString(item.desiredCount())
                : "";
        contextMenuIdentity = null;
        contextMenuKitId = null;
        contextMenuChestStorageId = null;
        editingIslandId = null;
        status = "desired count";
        rebuildRequested = true;
    }

    private void beginIslandEdit(SlotWorkspaceViewModel.AtlasIsland island, float screenX, float screenY) {
        if (island == null || island.kind() != VisualAtlasIslandKind.PLAYER) {
            setStatus("section cannot be edited");
            return;
        }
        editingIslandId = island.islandId();
        islandLabelDraft = island.label();
        islandEditX = screenX;
        islandEditY = screenY;
        contextMenuIdentity = null;
        contextMenuKitId = null;
        contextMenuChestStorageId = null;
        editingDesiredCountIdentity = null;
        renamingKitId = null;
        renameKitDraft = "";
        confirmDeleteKitId = null;
        renamingChestStorageId = null;
        renameChestDraft = "";
        status = "editing " + island.label();
        rebuildRequested = true;
    }

    private void beginKitRenameEdit(SlotWorkspaceViewModel.KitCard kit) {
        if (kit == null || kit.kitId().isBlank()) {
            setStatus("missing kit");
            return;
        }
        renamingKitId = kit.kitId();
        renameKitDraft = kit.name();
        confirmDeleteKitId = null;
        status = "renaming kit";
        rebuildRequested = true;
    }

    private void beginChestRenameEdit(SlotWorkspaceViewModel.ChestChip chip) {
        if (chip == null || chip.storageId().isBlank()) {
            setStatus("missing chest");
            return;
        }
        renamingChestStorageId = chip.storageId();
        renameChestDraft = chip.label();
        status = "renaming chest";
        rebuildRequested = true;
    }

    private boolean handleEditorKey(int keyCode) {
        if (editingDesiredCountIdentity == null
                && editingIslandId == null
                && renamingKitId == null
                && renamingChestStorageId == null) {
            return false;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            closeOverlays();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            if (editingDesiredCountIdentity != null) {
                commitDesiredCountEdit();
            } else if (editingIslandId != null) {
                commitIslandNameEdit();
            } else if (renamingKitId != null) {
                commitKitRenameEdit();
            } else if (renamingChestStorageId != null) {
                commitChestRenameEdit();
            }
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (editingDesiredCountIdentity != null) {
                if (!desiredCountDraft.isEmpty()) {
                    desiredCountDraft = desiredCountDraft.substring(0, desiredCountDraft.length() - 1);
                    rebuildRequested = true;
                }
            } else if (editingIslandId != null && !islandLabelDraft.isEmpty()) {
                islandLabelDraft = islandLabelDraft.substring(0, islandLabelDraft.length() - 1);
                rebuildRequested = true;
            } else if (renamingKitId != null && !renameKitDraft.isEmpty()) {
                renameKitDraft = renameKitDraft.substring(0, renameKitDraft.length() - 1);
                rebuildRequested = true;
            } else if (renamingChestStorageId != null && !renameChestDraft.isEmpty()) {
                renameChestDraft = renameChestDraft.substring(0, renameChestDraft.length() - 1);
                rebuildRequested = true;
            }
            return true;
        }
        return false;
    }

    private boolean handleEditorChar(char codePoint) {
        if (editingDesiredCountIdentity == null
                && editingIslandId == null
                && renamingKitId == null
                && renamingChestStorageId == null) {
            return false;
        }
        if (editingDesiredCountIdentity != null) {
            if (codePoint >= '0' && codePoint <= '9' && desiredCountDraft.length() < 4) {
                desiredCountDraft += codePoint;
                rebuildRequested = true;
            }
            return true;
        }
        if (renamingKitId != null) {
            if (isPrintable(codePoint) && renameKitDraft.length() < 32) {
                renameKitDraft += codePoint;
                rebuildRequested = true;
            }
            return true;
        }
        if (renamingChestStorageId != null) {
            if (isPrintable(codePoint) && renameChestDraft.length() < 32) {
                renameChestDraft += codePoint;
                rebuildRequested = true;
            }
            return true;
        }
        if (isPrintable(codePoint) && islandLabelDraft.length() < 32) {
            islandLabelDraft += codePoint;
            rebuildRequested = true;
        }
        return true;
    }

    private void commitDesiredCountEdit() {
        SlotWorkspaceViewModel.IdentityRef identity = editingDesiredCountIdentity;
        if (identity == null) {
            closeOverlays();
            return;
        }
        int count;
        try {
            count = desiredCountDraft.isBlank() ? 0 : Integer.parseInt(desiredCountDraft);
        } catch (NumberFormatException exception) {
            setStatus("desired count must be a number");
            return;
        }
        closeOverlayState();
        sendIdentityRefAction(WorkspaceActionId.SET_PLAYER_DESIRED_COUNT, identity, "desired count updated", count);
    }

    private void commitIslandNameEdit() {
        if (editingIslandId == null) {
            closeOverlays();
            return;
        }
        String label = islandLabelDraft == null ? "" : islandLabelDraft.trim();
        if (label.isBlank()) {
            setStatus("section name required");
            return;
        }
        String islandId = editingIslandId;
        closeOverlayState();
        sendAction(WorkspaceActionId.RENAME_ISLAND, "renaming section", islandId, label);
    }

    private void commitKitRenameEdit() {
        if (renamingKitId == null) {
            closeOverlays();
            return;
        }
        String label = renameKitDraft == null ? "" : renameKitDraft.trim();
        if (label.isBlank()) {
            setStatus("kit name required");
            return;
        }
        String kitId = renamingKitId;
        closeOverlayState();
        sendKitAction(WorkspaceActionId.RENAME_KIT, "renaming kit", kitId, label);
    }

    private void commitChestRenameEdit() {
        if (renamingChestStorageId == null) {
            closeOverlays();
            return;
        }
        String label = renameChestDraft == null ? "" : renameChestDraft.trim();
        if (label.isBlank()) {
            setStatus("chest name required");
            return;
        }
        String storageId = renamingChestStorageId;
        closeOverlayState();
        sendAction(WorkspaceActionId.RELABEL_CHEST, "renaming chest", storageId, label);
    }

    private void closeOverlays() {
        closeOverlayState();
        rebuildRequested = true;
    }

    private void closeOverlayState() {
        contextMenuIdentity = null;
        contextMenuKitId = null;
        contextMenuChestStorageId = null;
        editingIslandId = null;
        editingDesiredCountIdentity = null;
        islandLabelDraft = "";
        desiredCountDraft = "";
        renamingKitId = null;
        renameKitDraft = "";
        confirmDeleteKitId = null;
        renamingChestStorageId = null;
        renameChestDraft = "";
    }

    private static boolean isPrintable(char codePoint) {
        return codePoint >= 32 && codePoint != 127;
    }

    private static String shorten(String text, int maxChars) {
        String value = text == null ? "" : text;
        int max = Math.max(1, maxChars);
        if (value.length() <= max) {
            return value;
        }
        if (max <= 3) {
            return value.substring(0, max);
        }
        return value.substring(0, max - 3) + "...";
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

    private boolean sendAssignHome(
            SlotWorkspaceViewModel.IdentityRef identity,
            String islandId,
            Integer ordinal,
            String sentStatus
    ) {
        if (identity == null || islandId == null || islandId.isBlank()) {
            setStatus("invalid home target");
            return false;
        }
        boolean sent = actionChannel.send(
                WorkspaceActionId.ASSIGN_HOME,
                identity.itemId(),
                identity.comparisonMode(),
                identity.componentFingerprint(),
                islandId,
                ordinal);
        if (sent) {
            rememberRehomeTarget(islandId);
        }
        status = sent ? sentStatus : "failed to send assign_home";
        rebuildRequested = true;
        return sent;
    }

    private void rememberRehomeTarget(String islandId) {
        if (islandId == null || islandId.isBlank()) {
            return;
        }
        recentRehomeIslandIds.remove(islandId);
        recentRehomeIslandIds.addFirst(islandId);
        while (recentRehomeIslandIds.size() > RECENT_REHOME_CAPACITY) {
            recentRehomeIslandIds.removeLast();
        }
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
        return cardGestureContext(item, button, shiftDown, Screen.hasControlDown());
    }

    private WallCardTransferGesturePolicy.Context cardGestureContext(
            SlotWorkspaceViewModel.AtlasItem item,
            int button,
            boolean shiftDown,
            boolean controlDown
    ) {
        return new WallCardTransferGesturePolicy.Context(
                item,
                button,
                shiftDown,
                controlDown,
                cursorIdentity(),
                isCursorCarrying(),
                mode == Mode.SIDEBAR,
                carriedFreeSlotCount(),
                anyChestProximate(),
                shiftClickTransferState.continuingTake(
                        item == null ? null : item.identity(),
                        shiftDown));
    }

    private int wheelSteps(
            SlotWorkspaceViewModel.IdentityRef identity,
            float delta,
            boolean controlDown
    ) {
        if (delta == 0f) {
            return 0;
        }
        if (controlDown) {
            if (identity != null) {
                wheelAccumulatorByIdentity.remove(identity);
            }
            return delta > 0f ? 1 : -1;
        }
        if (identity == null) {
            return (int) delta;
        }
        float accumulated = wheelAccumulatorByIdentity.getOrDefault(identity, 0f) + delta;
        int steps = (int) accumulated;
        float remainder = accumulated - steps;
        rememberWheelRemainder(identity, remainder);
        return steps;
    }

    private void rememberWheelRemainder(SlotWorkspaceViewModel.IdentityRef identity, float remainder) {
        if (identity == null) {
            return;
        }
        if (Math.abs(remainder) < 0.0001f) {
            wheelAccumulatorByIdentity.remove(identity);
            return;
        }
        wheelAccumulatorByIdentity.put(identity, remainder);
        while (wheelAccumulatorByIdentity.size() > WHEEL_ACCUMULATOR_MAX_IDENTITIES) {
            var iterator = wheelAccumulatorByIdentity.keySet().iterator();
            if (!iterator.hasNext()) {
                return;
            }
            iterator.next();
            iterator.remove();
        }
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
            case TAKE_DESIRED_GAP_OR_STACK_BY_IDENTITY -> sendIdentityAction(
                    WorkspaceActionId.TAKE_DESIRED_GAP_OR_STACK_BY_IDENTITY,
                    item,
                    "taking " + item.name());
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
        shiftClickTransferState.record(decision, item == null ? null : item.identity(), Screen.hasShiftDown());
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
        sendIdentityRefAction(action, item.identity(), sentStatus, tail);
    }

    private void sendIdentityRefAction(
            WorkspaceActionId action,
            SlotWorkspaceViewModel.IdentityRef identity,
            String sentStatus,
            Object... tail
    ) {
        if (identity == null) {
            setStatus("missing item identity");
            return;
        }
        Object[] args = new Object[3 + (tail == null ? 0 : tail.length)];
        args[0] = identity.itemId();
        args[1] = identity.comparisonMode();
        args[2] = identity.componentFingerprint();
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

        @Override
        public void openChestMenu(String storageId, float screenX, float screenY) {
            ForgeWorkspaceSurface.this.openChestContextMenu(storageId, screenX, screenY);
        }
    }

    private final class HeaderContext implements WallSectionHeaderUiBuilder.Context {
        @Override
        public void beginIslandEdit(SlotWorkspaceViewModel.AtlasIsland island, float screenX, float screenY) {
            ForgeWorkspaceSurface.this.beginIslandEdit(island, screenX, screenY);
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

        @Override
        public void openKitMenu(String kitId, float screenX, float screenY) {
            ForgeWorkspaceSurface.this.openKitContextMenu(kitId, screenX, screenY);
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
            if (item != null && tree != null) {
                tree.scrollToElementId(item.islandId());
            }
            setStatus(item == null ? "ready" : item.name());
        }

        @Override
        public void hoverRecent(SlotWorkspaceViewModel.AtlasItem item) {
            hoveredIdentity = item == null ? null : item.identity();
        }

        @Override
        public void clearHoveredRecent(SlotWorkspaceViewModel.AtlasItem item) {
            if (item != null && item.identity().equals(hoveredIdentity)) {
                hoveredIdentity = null;
            }
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
