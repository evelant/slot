package dev.imagio.slot.neoforge.screen.ldlib;

import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceFormat.*;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceTheme.*;
import static dev.imagio.slot.neoforge.screen.ldlib.WorkspaceUi.*;

import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.DataBindingBuilder;
import com.lowdragmc.lowdraglib2.gui.sync.rpc.RPCEmitter;
import com.lowdragmc.lowdraglib2.gui.sync.rpc.RPCEventBuilder;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.BindableValue;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import com.lowdragmc.lowdraglib2.gui.ui.event.HoverTooltips;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.util.DrawerHelper;
import dev.imagio.slot.atlas.AtlasSearchIndex;
import dev.imagio.slot.atlas.FitCarriedCamera;
import dev.imagio.slot.atlas.lod.AtlasLayout;
import dev.imagio.slot.atlas.lod.AtlasLayoutConfig;
import dev.imagio.slot.atlas.lod.AtlasLayoutResult;
import dev.imagio.slot.atlas.lod.AtlasRelevance;
import dev.imagio.slot.atlas.lod.Band;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.AtlasItemDrag;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.ChestStackDrag;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.ChestTileDrag;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.HotbarSlotDrag;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.IslandDrag;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.KitBringDrag;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.KitSlotDrag;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.StorageZoneBounds;
import dev.imagio.slot.neoforge.screen.ldlib.WorkspaceDrags.StorageZoneDrag;
import dev.imagio.slot.neoforge.screen.ldlib.util.Observable;
import dev.imagio.slot.inventory.triage.ChipSuggestion;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceAtlasLayout;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.workflow.domain.KitPage;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import dev.vfyjxf.taffy.style.AlignContent;
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
import java.util.Optional;
import java.util.Set;

final class SlotWorkspaceUiController {
    final SlotWorkspaceUiSession session;
    final Player player;
    final UIElement root;
    final UIElement content;

    SlotWorkspaceViewModel viewModel;
    /**
     * Latest client-side atlas layout, recomputed every refresh from
     * {@link #viewModel} + the active search query. Per
     * {@code docs/decisions/0005-relevance-score-and-layout-locality.md},
     * positions and cell sizes for atlas items live here, not on
     * {@link SlotWorkspaceViewModel.AtlasItem}. Use
     * {@link #placementFor(SlotWorkspaceViewModel.AtlasItem)} to look up.
     */
    AtlasLayoutResult currentLayout = AtlasLayoutResult.EMPTY;
    final Observable<String> localStatus = new Observable<>("");
    final Observable<SlotWorkspaceViewModel.IdentityRef> selectedAtlasIdentity = new Observable<>(null);
    SlotWorkspaceViewModel.IdentityRef hoveredAtlasIdentity;
    String hoveredIslandId;
    /**
     * Storage areas the player has explicitly pinned-expanded by clicking
     * their chip. Phase 3 of {@code docs/plans/storage-areas.md}: areas
     * default-collapsed when nothing is proximate; the user can pin one
     * open to inspect contents off-base. Persists across rebuilds within
     * a session — a fresh screen open starts with everything collapsed.
     */
    final java.util.Set<String> expandedAreaIds = new java.util.HashSet<>();
    final Observable<Integer> selectedHotbarIndex = new Observable<>(-1);
    int hoveredHotbarIndex = -1;
    final List<Observable.Subscription> atlasContentSubscriptions = new ArrayList<>();
    final java.util.Map<Integer, UIElement> hotbarSlotElements = new java.util.HashMap<>();
    /**
     * Dim non-proximate link-thread elements, keyed by island id. Built
     * once per atlas rebuild for every chest-linked island that has at
     * least one non-proximate linked chest, hidden by default, and
     * toggled visible on hover via
     * {@link IslandChestBuilder#attachIslandHoverListeners}. Pre-creating
     * the elements lets the hover handler call {@code setVisible} without
     * forcing a full {@link #rebuildNow()} — a rebuild on hover would
     * destroy the element holding the listener mid-event, fire a
     * synthetic MOUSE_LEAVE on the destroyed element, fire MOUSE_ENTER on
     * the freshly recreated one, and oscillate every frame. That loop is
     * what previously made chest-linked island headers undraggable.
     */
    final java.util.Map<String, java.util.List<UIElement>> dimLinkThreadsByIsland = new java.util.HashMap<>();
    SlotWorkspaceViewModel.IdentityRef contextMenuAtlasIdentity;
    int contextMenuHotbarIndex = -1;
    String contextMenuKitId;
    String renamingKitId;
    String renameKitDraft = "";
    String confirmDeleteKitId;
    float contextMenuScreenX;
    float contextMenuScreenY;
    final java.util.ArrayDeque<String> recentRehomeIslandIds = new java.util.ArrayDeque<>();
    static final int RECENT_REHOME_MAX_DISPLAYED = 3;
    static final int RECENT_REHOME_CAPACITY = 6;
    String editingIslandId = null;
    String editingChestStorageId = null;
    String islandLabelDraft = "";
    float islandEditScreenX = Float.NaN;
    float islandEditScreenY = Float.NaN;
    SlotWorkspaceViewModel.IdentityRef pendingCreateIdentity;
    int pendingCreateWorldX;
    int pendingCreateWorldY;
    String pendingCreateLabel = "";
    int pendingCreateColor = ISLAND_PALETTE[0];
    boolean pendingCreateFocusPending;

    boolean kitRackOpen;
    AtlasCamera atlasCamera;
    final AtlasCameraController cameraController = new AtlasCameraController();
    final SearchController searchController = new SearchController(this);
    final WorkspaceRpcDispatcher rpc = new WorkspaceRpcDispatcher(this);
    final DragDropWiring drag = new DragDropWiring(this);
    final HotkeyRouter hotkeys = new HotkeyRouter(this);
    final CameraNavigator camera = new CameraNavigator(this);
    final WorkspaceOverlays overlays = new WorkspaceOverlays(this);
    final AtlasPanelBuilder atlasPanel = new AtlasPanelBuilder(this);
    final TriagePanelBuilder triagePanel = new TriagePanelBuilder(this);
    final BeltPanelBuilder belt = new BeltPanelBuilder(this);
    final KitRackBuilder kit = new KitRackBuilder(this);
    final ContextMenuBuilder menu = new ContextMenuBuilder(this);
    final IslandChestBuilder islandChest = new IslandChestBuilder(this);
    final AtlasCardBuilder atlasCard = new AtlasCardBuilder(this);
    SlotAtlasGraphView atlasView;
    UIElement atlasPanelElement;
    UIElement hoverTrailOverlayElement;
    UIElement carriedFreeSlotsChipElement;
    UIElement topRightActionsElement;
    UIElement statusBarElement;
    Label statusBarLabel;
    boolean atlasContentNeedsScreenTick;
    // Deferred rebuild flag. Every server-sync round trip calls rebuild()
    // via syncBinding's remoteSetter; during rapid bursts (e.g. scroll-
    // wheel item transfer firing N RPCs) this used to destroy and recreate
    // the entire atlas content subtree N times before a single TICK could
    // warm up the new elements, which caused visible font-size, selection,
    // and hotbar-highlight flicker. With this flag, rebuild() just marks
    // the UI dirty; flushRebuildIfPending() in the per-frame tick
    // collapses any number of requests into one actual rebuild per frame.
    boolean rebuildPending;
    SlotWorkspaceViewModel.IdentityRef hoveredChestCellIdentity;
    String hoveredChestCellStorageId;
    // Set by drop targets that handle a ChestStackDrag for something OTHER
    // than "take the item into inventory" (e.g. island assign-home is a
    // pure metadata op — item stays in the chest). The chest cell's
    // DRAG_END reads this to decide whether its default sendTakeFromChest
    // should fire. Reset in DRAG_END regardless.
    boolean chestDragDropConsumed;
    boolean peekActive;
    long peekPressTimeMs;
    AtlasCamera peekTarget;
    String gatherKitId = "";
    int gatherStep = 0;

    SlotWorkspaceUiController(SlotWorkspaceUiSession session, Player player) {
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

    ModularUI create() {
        rpc.register();
        hotkeys.installBeltHotkeys();
        root.addChildren(syncBinding(), content);
        rebuildNow();
        return ModularUI.of(UI.of(root), player);
    }


















    UIElement syncBinding() {
        BindableValue<Tag> binding = new BindableValue<>();
        binding.bind(DataBindingBuilder.tagS2C(session::viewTag)
                .remoteSetter(tag -> {
                    session.acceptRemoteView(tag);
                    viewModel = session.viewModel();
                    dev.imagio.slot.neoforge.client.SlotClientWorkspaceCache.update(viewModel);
                    localStatus.set("");
                    rebuild();
                })
                .build());
        binding.layout(layout -> layout.width(0).height(0));
        return binding;
    }

    void rebuild() {
        rebuildPending = true;
    }

    void flushRebuildIfPending() {
        if (rebuildPending) {
            rebuildNow();
        }
    }

    /**
     * Re-run the client-side atlas layout pass. Must be called any
     * time the inputs change: a new view model arrives, the search
     * query is submitted/cleared, the kit activates, etc. Reads the
     * search query from {@link #searchController}; cheap enough to
     * call every {@link #rebuildNow()} for now (revisit if hot).
     */
    void recomputeLayout() {
        currentLayout = AtlasLayout.layout(
                viewModel,
                searchController.normalizedQuery(),
                AtlasRelevance.DEFAULT_CONTRIBUTORS,
                AtlasLayoutConfig.DEFAULT
        );
    }

    /**
     * World-space placement for an atlas item under the current
     * layout. Falls back to a baseline placeholder when the item
     * isn't part of the latest layout (e.g., triage items, freshly
     * arrived items the layout pass hasn't yet seen).
     */
    AtlasLayoutResult.ItemPlacement placementFor(SlotWorkspaceViewModel.AtlasItem item) {
        if (item == null) {
            return PLACEMENT_FALLBACK;
        }
        AtlasLayoutResult.ItemPlacement placement = currentLayout.placementOf(item.identity());
        return placement == null ? fallbackPlacement(item) : placement;
    }

    private static final AtlasLayoutResult.ItemPlacement PLACEMENT_FALLBACK =
            new AtlasLayoutResult.ItemPlacement(
                    "",
                    0,
                    0,
                    SlotWorkspaceAtlasLayout.CARD_WIDTH,
                    SlotWorkspaceAtlasLayout.CARD_HEIGHT,
                    0f
            );

    /**
     * World-space placement for an island under the current layout.
     * Falls back to the island's authored origin with a baseline empty
     * footprint when the island isn't part of the latest layout (Triage
     * island, transient view-model state).
     */
    AtlasLayoutResult.IslandPlacement islandPlacementFor(SlotWorkspaceViewModel.AtlasIsland island) {
        if (island == null) {
            return null;
        }
        AtlasLayoutResult.IslandPlacement placement = currentLayout.islandPlacementOf(island.islandId());
        if (placement != null) {
            return placement;
        }
        return new AtlasLayoutResult.IslandPlacement(
                island.islandId(),
                island.x(),
                island.y(),
                SlotWorkspaceAtlasLayout.PLAYER_ISLAND_MIN_WIDTH,
                SlotWorkspaceAtlasLayout.PLAYER_ISLAND_MIN_HEIGHT,
                island.itemCount()
        );
    }

    private static AtlasLayoutResult.ItemPlacement fallbackPlacement(SlotWorkspaceViewModel.AtlasItem item) {
        // Sane default for items the layout pass hasn't (yet) seen — Triage
        // items, freshly-arrived items, etc. Coordinates are zero-relative;
        // callers that need world positions should be reading from
        // {@link AtlasLayoutResult} directly (this fallback is for inner-card
        // sizing math that only consumes width/height).
        return new AtlasLayoutResult.ItemPlacement(
                item.islandId(),
                0,
                0,
                SlotWorkspaceAtlasLayout.CARD_WIDTH,
                SlotWorkspaceAtlasLayout.CARD_HEIGHT,
                0f
        );
    }

    void rebuildNow() {
        rebuildPending = false;
        recomputeLayout();
        if (selectedAtlasIdentity.get() != null && viewModel.atlasItem(selectedAtlasIdentity.get()) == null) {
            selectedAtlasIdentity.set(null);
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
        if (!contentPopulated) {
            // First build only: create the body/statusBar wrappers and add
            // them to content. Subsequent rebuilds reuse the same wrappers
            // — replacing them caused a blank-frame flash, because
            // rebuildNow runs inside atlas.perFrameTick (i.e. inside
            // atlas.drawBackgroundTexture), which is *after* the parent
            // content element has already drawn this frame with the old
            // children. Replacing the children means next frame renders
            // NEW elements whose layout hasn't settled yet — visible as a
            // 1-frame mismatch / flash.
            content.clearAllChildren();
            content.addChildren(
                    atlasPanel.body(),
                    overlays.statusBar()
            );
            contentPopulated = true;
        } else {
            // Incremental refresh: atlasPanel.atlasPanel() is the persistent panel
            // inside body, and calling it reruns atlasPanel.repopulateAtlasPanel()
            // which destroys+rebuilds just the atlas-content subtree
            // (islands/cards/chest tiles). That subtree is what the
            // server sync actually invalidates.
            atlasPanel.atlasPanel();
        }
        content.markTaffyStyleDirty();
    }

    boolean contentPopulated;


    void installKeybindTooltip(
            Button button,
            String action,
            java.util.function.Supplier<String> keyLabelSupplier
    ) {
        button.addEventListener(UIEvents.HOVER_TOOLTIPS, event -> {
            String binding = "";
            try {
                binding = keyLabelSupplier.get();
            } catch (RuntimeException ignored) {
            }
            String tooltipText = binding == null || binding.isBlank()
                    ? action
                    : action + " (" + binding + ")";
            event.hoverTooltips = new HoverTooltips(
                    List.of(Component.literal(tooltipText)),
                    null,
                    null,
                    null
            );
        });
    }













    boolean shouldAccentHotbarSlot(SlotWorkspaceViewModel.HotbarSlot slot) {
        if (hoveredAtlasIdentity == null) {
            return false;
        }
        SlotWorkspaceViewModel.IdentityRef slotIdentity = SlotWorkspaceViewModel.IdentityRef.from(
                dev.imagio.slot.inventory.core.ItemIdentityMatcher.create(slot.displayStack()));
        return hoveredAtlasIdentity.equals(slotIdentity);
    }

    boolean shouldAccentTriageRow(SlotWorkspaceViewModel.AtlasItem item) {
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







    static boolean chipMatches(SlotWorkspaceViewModel.AtlasItem item, ChipSuggestion target) {
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

    static String chipLabelText(ChipSuggestion chip) {
        // No hard truncation — chip labels live inside flex(1) layouts that
        // soft-clip when the container is narrower than the text. A fixed
        // 10-char cap was ellipsizing well-formed labels (e.g. "Mechanis...")
        // even when the surrounding row had plenty of horizontal slack.
        String label = chip.kind() == ChipSuggestion.ChipKind.TEMPLATE && chip.template() != null
                ? chip.template().defaultLabel()
                : chip.label();
        return label == null ? "" : label;
    }








    int firstFreeHotbarIndex() {
        for (SlotWorkspaceViewModel.HotbarSlot s : viewModel.hotbarSlots()) {
            if (!s.occupied()) {
                return s.hotbarIndex();
            }
        }
        return -1;
    }

    boolean atlasItemHasDepositTarget(SlotWorkspaceViewModel.AtlasItem item) {
        if (item == null || item.islandId() == null || item.islandId().isBlank()) {
            return false;
        }
        for (SlotWorkspaceViewModel.ClaimedChestTile tile : viewModel.claimedChestTiles()) {
            if (tile.proximate() && tile.linkedIslandIds().contains(item.islandId())) {
                return true;
            }
        }
        return false;
    }













    void installHotbarHoverTooltip(Button button, SlotWorkspaceViewModel.HotbarSlot slot) {
        if (!slot.occupied()) {
            return;
        }
        WorkspaceUi.installItemTooltip(button, slot.displayStack());
    }

    void installOffhandHoverTooltip(Button button, SlotWorkspaceViewModel.OffhandSlot offhand) {
        if (!offhand.occupied()) {
            return;
        }
        WorkspaceUi.installItemTooltip(button, offhand.displayStack());
    }






    void rememberRehomeTarget(String islandId) {
        if (islandId == null || islandId.isBlank()) {
            return;
        }
        if (SlotWorkspaceAtlasLayout.ISLAND_TRIAGE.equals(islandId)) {
            return;
        }
        recentRehomeIslandIds.remove(islandId);
        recentRehomeIslandIds.addFirst(islandId);
        while (recentRehomeIslandIds.size() > RECENT_REHOME_CAPACITY) {
            recentRehomeIslandIds.removeLast();
        }
    }

    List<SlotWorkspaceViewModel.AtlasIsland> recentRehomeTargets(SlotWorkspaceViewModel.AtlasItem forItem) {
        ArrayList<SlotWorkspaceViewModel.AtlasIsland> result = new ArrayList<>();
        String currentIsland = forItem == null ? "" : forItem.islandId();
        for (String islandId : recentRehomeIslandIds) {
            if (islandId.equals(currentIsland)) {
                continue;
            }
            SlotWorkspaceViewModel.AtlasIsland island = viewModel.island(islandId);
            if (island == null) {
                continue;
            }
            result.add(island);
            if (result.size() >= RECENT_REHOME_MAX_DISPLAYED) {
                break;
            }
        }
        return result;
    }




    // Identity-based hotbar assignment. Superseded the old sendTransfer
    // path for atlas-item → hotbar-slot moves because slot-index-based
    // transfers assumed PLAYER_MAIN, which isn't where the item lives
    // when it's in a carried backpack.




    int hotbarSlotForIdentity(SlotWorkspaceViewModel.IdentityRef identity) {
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





    boolean anyChestProximate() {
        for (SlotWorkspaceViewModel.ClaimedChestTile tile : viewModel.claimedChestTiles()) {
            if (tile.proximate()) {
                return true;
            }
        }
        return false;
    }






    SlotWorkspaceViewModel.AtlasItem hoveredAtlasItem() {
        return viewModel.atlasItem(hoveredAtlasIdentity);
    }

    SlotWorkspaceViewModel.AtlasItem selectedAtlasItem() {
        return viewModel.atlasItem(selectedAtlasIdentity.get());
    }

    SlotWorkspaceViewModel.AtlasItem focusedAtlasItem() {
        SlotWorkspaceViewModel.AtlasItem selected = selectedAtlasItem();
        return selected != null ? selected : hoveredAtlasItem();
    }

    SlotWorkspaceViewModel.IdentityRef currentMapFocusIdentity() {
        if (hoveredAtlasIdentity != null && viewModel.atlasItem(hoveredAtlasIdentity) != null) {
            return hoveredAtlasIdentity;
        }
        return selectedAtlasIdentity.get();
    }

    boolean isMapFocusItem(SlotWorkspaceViewModel.AtlasItem item) {
        SlotWorkspaceViewModel.IdentityRef focusIdentity = currentMapFocusIdentity();
        return item != null && focusIdentity != null && item.identity().equals(focusIdentity);
    }

    SlotWorkspaceViewModel.HotbarSlot selectedHotbarSlot() {
        int idx = selectedHotbarIndex.get();
        if (idx < 0 || idx >= viewModel.hotbarSlots().size()) {
            return null;
        }
        SlotWorkspaceViewModel.HotbarSlot slot = viewModel.hotbarSlots().get(idx);
        return slot.occupied() ? slot : null;
    }

    String selectionLabel() {
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

    void appendTooltipPreview(List<UIElement> children, SlotWorkspaceViewModel.AtlasItem item) {
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






    void clearSelectionOnDirectClick(UIElement element) {
        element.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (event.target == element && (selectedAtlasIdentity.get() != null || selectedHotbarIndex.get() >= 0)) {
                selectedAtlasIdentity.set(null);
                selectedHotbarIndex.set(-1);
                localStatus.set("selection cleared");
            }
        });
    }
}
