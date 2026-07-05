package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.workflow.domain.CraftRunState;

import java.util.List;
import java.util.Set;

/**
 * Projection slices that match the existing encoded view-model slices.
 */
final class WorkspaceProjectedSlices {
    static final int SLICE_COUNT = 7;

    private final FrameSlice frame;
    private final WallSlice wall;
    private final StorageSlice storage;
    private final HotbarSlice hotbar;
    private final WorkflowSlice workflow;
    private final PanelsSlice panels;
    private final ContextualSlice contextual;

    private WorkspaceProjectedSlices(
            FrameSlice frame,
            WallSlice wall,
            StorageSlice storage,
            HotbarSlice hotbar,
            WorkflowSlice workflow,
            PanelsSlice panels,
            ContextualSlice contextual
    ) {
        this.frame = frame == null ? FrameSlice.empty() : frame;
        this.wall = wall == null ? WallSlice.empty() : wall;
        this.storage = storage == null ? StorageSlice.empty() : storage;
        this.hotbar = hotbar == null ? HotbarSlice.empty() : hotbar;
        this.workflow = workflow == null ? WorkflowSlice.empty() : workflow;
        this.panels = panels == null ? PanelsSlice.empty() : panels;
        this.contextual = contextual == null ? ContextualSlice.empty() : contextual;
    }

    static WorkspaceProjectedSlices from(SlotWorkspaceViewModel viewModel) {
        SlotWorkspaceViewModel resolved = viewModel == null ? SlotWorkspaceViewModel.empty() : viewModel;
        return new WorkspaceProjectedSlices(
                new FrameSlice(
                        resolved.status(),
                        resolved.diagnostics(),
                        resolved.pendingCount(),
                        resolved.selectedQuickAccessSlot()),
                new WallSlice(
                        resolved.canvasWidth(),
                        resolved.canvasHeight(),
                        resolved.carriedFreeSlotCount(),
                        resolved.carriedSlotCapacity(),
                        resolved.islands(),
                        resolved.atlasItems(),
                        resolved.triageItems()),
                new StorageSlice(
                        resolved.chestChips(),
                        resolved.chestClusters(),
                        resolved.wayfindingTargets(),
                        resolved.depositableIdentities()),
                new HotbarSlice(
                        resolved.hotbarSlots(),
                        resolved.offhand(),
                        resolved.recentIdentities()),
                new WorkflowSlice(
                        resolved.kits(),
                        resolved.craftRun()),
                new PanelsSlice(
                        resolved.lootChestPanel(),
                        resolved.activeChestPanel()),
                new ContextualSlice(resolved.contextualSuggestionLanes()));
    }

    SlotWorkspaceViewModel compose(long revision) {
        return new SlotWorkspaceViewModel(
                revision,
                frame.status(),
                frame.diagnostics(),
                frame.pendingCount(),
                frame.selectedQuickAccessSlot(),
                wall.canvasWidth(),
                wall.canvasHeight(),
                wall.carriedFreeSlotCount(),
                wall.carriedSlotCapacity(),
                wall.islands(),
                wall.atlasItems(),
                wall.triageItems(),
                storage.chestChips(),
                storage.chestClusters(),
                hotbar.hotbarSlots(),
                hotbar.offhand(),
                workflow.kits(),
                panels.lootChestPanel(),
                storage.wayfindingTargets(),
                storage.depositableIdentities(),
                hotbar.recentIdentities(),
                panels.activeChestPanel(),
                workflow.craftRun(),
                contextual.contextualSuggestionLanes());
    }

    ReuseResult reuseAgainst(WorkspaceViewSliceKeys previousKeys, WorkspaceProjectedSlices previous, WorkspaceViewSliceKeys nextKeys) {
        if (previousKeys == null || previous == null || nextKeys == null) {
            return new ReuseResult(this, new WorkspaceProjectionSliceStats(0, SLICE_COUNT));
        }
        int reused = 0;
        int rebuilt = 0;
        FrameSlice nextFrame = frame;
        if (previousKeys.frame().equals(nextKeys.frame())) {
            nextFrame = previous.frame;
            reused++;
        } else {
            rebuilt++;
        }
        WallSlice nextWall = wall;
        if (previousKeys.wall().equals(nextKeys.wall())) {
            nextWall = previous.wall;
            reused++;
        } else {
            rebuilt++;
        }
        StorageSlice nextStorage = storage;
        if (previousKeys.storage().equals(nextKeys.storage())) {
            nextStorage = previous.storage;
            reused++;
        } else {
            rebuilt++;
        }
        HotbarSlice nextHotbar = hotbar;
        if (previousKeys.hotbar().equals(nextKeys.hotbar())) {
            nextHotbar = previous.hotbar;
            reused++;
        } else {
            rebuilt++;
        }
        WorkflowSlice nextWorkflow = workflow;
        if (previousKeys.workflow().equals(nextKeys.workflow())) {
            nextWorkflow = previous.workflow;
            reused++;
        } else {
            rebuilt++;
        }
        PanelsSlice nextPanels = panels;
        if (previousKeys.panels().equals(nextKeys.panels())) {
            nextPanels = previous.panels;
            reused++;
        } else {
            rebuilt++;
        }
        ContextualSlice nextContextual = contextual;
        if (previousKeys.contextual().equals(nextKeys.contextual())) {
            nextContextual = previous.contextual;
            reused++;
        } else {
            rebuilt++;
        }
        return new ReuseResult(
                new WorkspaceProjectedSlices(
                        nextFrame,
                        nextWall,
                        nextStorage,
                        nextHotbar,
                        nextWorkflow,
                        nextPanels,
                        nextContextual),
                new WorkspaceProjectionSliceStats(reused, rebuilt));
    }

    record ReuseResult(
            WorkspaceProjectedSlices slices,
            WorkspaceProjectionSliceStats stats
    ) {
        ReuseResult {
            slices = slices == null
                    ? new WorkspaceProjectedSlices(null, null, null, null, null, null, null)
                    : slices;
            stats = stats == null ? WorkspaceProjectionSliceStats.empty() : stats;
        }
    }

    private record FrameSlice(
            String status,
            String diagnostics,
            int pendingCount,
            int selectedQuickAccessSlot
    ) {
        FrameSlice {
            status = status == null || status.isBlank() ? "ready" : status;
            diagnostics = diagnostics == null ? "" : diagnostics;
            pendingCount = Math.max(0, pendingCount);
        }

        static FrameSlice empty() {
            return new FrameSlice("ready", "", 0, -1);
        }
    }

    private record WallSlice(
            int canvasWidth,
            int canvasHeight,
            int carriedFreeSlotCount,
            int carriedSlotCapacity,
            List<SlotWorkspaceViewModel.AtlasIsland> islands,
            List<SlotWorkspaceViewModel.AtlasItem> atlasItems,
            List<SlotWorkspaceViewModel.AtlasItem> triageItems
    ) {
        WallSlice {
            canvasWidth = Math.max(1, canvasWidth);
            canvasHeight = Math.max(1, canvasHeight);
            carriedFreeSlotCount = Math.max(0, carriedFreeSlotCount);
            carriedSlotCapacity = Math.max(carriedFreeSlotCount, carriedSlotCapacity);
            islands = islands == null ? List.of() : List.copyOf(islands);
            atlasItems = atlasItems == null ? List.of() : List.copyOf(atlasItems);
            triageItems = triageItems == null ? List.of() : List.copyOf(triageItems);
        }

        static WallSlice empty() {
            SlotWorkspaceViewModel empty = SlotWorkspaceViewModel.empty();
            return new WallSlice(
                    empty.canvasWidth(),
                    empty.canvasHeight(),
                    0,
                    0,
                    empty.islands(),
                    List.of(),
                    List.of());
        }
    }

    private record StorageSlice(
            List<SlotWorkspaceViewModel.ChestChip> chestChips,
            List<SlotWorkspaceViewModel.ChestClusterDescriptor> chestClusters,
            List<WayfindingTarget> wayfindingTargets,
            Set<SlotWorkspaceViewModel.IdentityRef> depositableIdentities
    ) {
        StorageSlice {
            chestChips = chestChips == null ? List.of() : List.copyOf(chestChips);
            chestClusters = chestClusters == null ? List.of() : List.copyOf(chestClusters);
            wayfindingTargets = wayfindingTargets == null ? List.of() : List.copyOf(wayfindingTargets);
            depositableIdentities = depositableIdentities == null ? Set.of() : Set.copyOf(depositableIdentities);
        }

        static StorageSlice empty() {
            return new StorageSlice(List.of(), List.of(), List.of(), Set.of());
        }
    }

    private record HotbarSlice(
            List<SlotWorkspaceViewModel.HotbarSlot> hotbarSlots,
            SlotWorkspaceViewModel.OffhandSlot offhand,
            List<SlotWorkspaceViewModel.IdentityRef> recentIdentities
    ) {
        HotbarSlice {
            hotbarSlots = hotbarSlots == null ? List.of() : List.copyOf(hotbarSlots);
            offhand = offhand == null ? SlotWorkspaceViewModel.OffhandSlot.empty() : offhand;
            recentIdentities = recentIdentities == null ? List.of() : List.copyOf(recentIdentities);
        }

        static HotbarSlice empty() {
            return new HotbarSlice(
                    SlotWorkspaceViewModel.emptyHotbar(),
                    SlotWorkspaceViewModel.OffhandSlot.empty(),
                    List.of());
        }
    }

    private record WorkflowSlice(
            List<SlotWorkspaceViewModel.KitCard> kits,
            CraftRunState craftRun
    ) {
        WorkflowSlice {
            kits = kits == null ? List.of() : List.copyOf(kits);
            craftRun = craftRun == null ? CraftRunState.empty() : craftRun;
        }

        static WorkflowSlice empty() {
            return new WorkflowSlice(List.of(), CraftRunState.empty());
        }
    }

    private record PanelsSlice(
            SlotWorkspaceViewModel.LootChestPanel lootChestPanel,
            SlotWorkspaceViewModel.ActiveChestPanel activeChestPanel
    ) {
        PanelsSlice {
            lootChestPanel = lootChestPanel == null ? SlotWorkspaceViewModel.LootChestPanel.empty() : lootChestPanel;
            activeChestPanel = activeChestPanel == null ? SlotWorkspaceViewModel.ActiveChestPanel.empty() : activeChestPanel;
        }

        static PanelsSlice empty() {
            return new PanelsSlice(
                    SlotWorkspaceViewModel.LootChestPanel.empty(),
                    SlotWorkspaceViewModel.ActiveChestPanel.empty());
        }
    }

    private record ContextualSlice(
            List<SlotWorkspaceViewModel.ContextualSuggestionLane> contextualSuggestionLanes
    ) {
        ContextualSlice {
            contextualSuggestionLanes = contextualSuggestionLanes == null
                    ? List.of()
                    : List.copyOf(contextualSuggestionLanes);
        }

        static ContextualSlice empty() {
            return new ContextualSlice(List.of());
        }
    }
}
