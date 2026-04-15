package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.inventory.browse.InventoryBrowsePane;
import dev.imagio.slot.inventory.core.CraftingSurfaceDescriptor;
import dev.imagio.slot.inventory.core.EquipmentGroupDescriptor;
import dev.imagio.slot.inventory.core.InventoryPaneMembership;
import dev.imagio.slot.inventory.core.InventoryToolDescriptor;
import dev.imagio.slot.inventory.core.QuickAccessLaneDescriptor;
import dev.imagio.slot.inventory.core.ToolRegionDescriptor;
import dev.imagio.slot.workflow.domain.CollectionProjection;
import dev.imagio.slot.workflow.domain.RecentView;

import java.util.List;
import java.util.Objects;

public sealed interface InventoryWorkspaceSurface permits
        InventoryWorkspaceSurface.BrowsePaneSurface,
        InventoryWorkspaceSurface.QuickAccessSurface,
        InventoryWorkspaceSurface.EquipmentSurface,
        InventoryWorkspaceSurface.ToolSurface,
        InventoryWorkspaceSurface.CraftingSurface,
        InventoryWorkspaceSurface.WorkflowSurface,
        InventoryWorkspaceSurface.StatusSurface {

    String id();

    InventoryWorkspaceSurfaceKind kind();

    String diagnostics();

    record BrowsePaneSurface(
            String id,
            InventoryPaneMembership paneMembership,
            boolean primary,
            InventoryWorkspaceSubjectRef.BrowseRef surfaceRef,
            InventoryBrowsePane pane,
            String diagnostics
    ) implements InventoryWorkspaceSurface {
        public BrowsePaneSurface {
            id = id == null ? "" : id;
            paneMembership = paneMembership == null ? InventoryPaneMembership.CARRIED : paneMembership;
            surfaceRef = surfaceRef == null ? new InventoryWorkspaceSubjectRef.BrowseRef(
                    pane == null ? null : pane.subjectRef()
            ) : surfaceRef;
            diagnostics = diagnostics == null ? "" : diagnostics;
        }

        @Override
        public InventoryWorkspaceSurfaceKind kind() {
            return InventoryWorkspaceSurfaceKind.BROWSE_PANE;
        }
    }

    record QuickAccessSurface(
            String id,
            List<QuickAccessLaneSurface> lanes,
            String selectedLaneId,
            int selectedSlotIndex,
            String diagnostics
    ) implements InventoryWorkspaceSurface {
        public QuickAccessSurface {
            id = id == null ? "" : id;
            lanes = lanes == null ? List.of() : List.copyOf(lanes.stream().filter(Objects::nonNull).toList());
            selectedLaneId = selectedLaneId == null ? "" : selectedLaneId;
            selectedSlotIndex = Math.max(-1, selectedSlotIndex);
            diagnostics = diagnostics == null ? "" : diagnostics;
        }

        @Override
        public InventoryWorkspaceSurfaceKind kind() {
            return InventoryWorkspaceSurfaceKind.QUICK_ACCESS;
        }
    }

    record QuickAccessLaneSurface(
            QuickAccessLaneDescriptor descriptor,
            InventoryWorkspaceSubjectRef.QuickAccessLaneRef subjectRef,
            List<InventoryWorkspaceTargetSlot> slots
    ) {
        public QuickAccessLaneSurface {
            subjectRef = subjectRef == null ? new InventoryWorkspaceSubjectRef.QuickAccessLaneRef(
                    descriptor == null ? "" : descriptor.id()
            ) : subjectRef;
            slots = slots == null ? List.of() : List.copyOf(slots.stream().filter(Objects::nonNull).toList());
        }
    }

    record EquipmentSurface(
            String id,
            List<EquipmentGroupSurface> groups,
            String diagnostics
    ) implements InventoryWorkspaceSurface {
        public EquipmentSurface {
            id = id == null ? "" : id;
            groups = groups == null ? List.of() : List.copyOf(groups.stream().filter(Objects::nonNull).toList());
            diagnostics = diagnostics == null ? "" : diagnostics;
        }

        @Override
        public InventoryWorkspaceSurfaceKind kind() {
            return InventoryWorkspaceSurfaceKind.EQUIPMENT;
        }
    }

    record EquipmentGroupSurface(
            EquipmentGroupDescriptor descriptor,
            InventoryWorkspaceSubjectRef.EquipmentGroupRef subjectRef,
            List<InventoryWorkspaceTargetSlot> slots
    ) {
        public EquipmentGroupSurface {
            subjectRef = subjectRef == null ? new InventoryWorkspaceSubjectRef.EquipmentGroupRef(
                    descriptor == null ? "" : descriptor.id()
            ) : subjectRef;
            slots = slots == null ? List.of() : List.copyOf(slots.stream().filter(Objects::nonNull).toList());
        }
    }

    record ToolSurface(
            String id,
            InventoryToolDescriptor tool,
            InventoryWorkspaceSubjectRef.ToolRef subjectRef,
            boolean pinned,
            List<ToolRegionSurface> regions,
            String diagnostics
    ) implements InventoryWorkspaceSurface {
        public ToolSurface {
            id = id == null ? "" : id;
            subjectRef = subjectRef == null ? new InventoryWorkspaceSubjectRef.ToolRef(tool == null ? "" : tool.id()) : subjectRef;
            regions = regions == null ? List.of() : List.copyOf(regions.stream().filter(Objects::nonNull).toList());
            diagnostics = diagnostics == null ? "" : diagnostics;
        }

        @Override
        public InventoryWorkspaceSurfaceKind kind() {
            return InventoryWorkspaceSurfaceKind.TOOL;
        }
    }

    record ToolRegionSurface(
            ToolRegionDescriptor descriptor,
            List<InventoryWorkspaceTargetSlot> slots
    ) {
        public ToolRegionSurface {
            slots = slots == null ? List.of() : List.copyOf(slots.stream().filter(Objects::nonNull).toList());
        }
    }

    record CraftingSurface(
            String id,
            String toolId,
            CraftingSurfaceDescriptor descriptor,
            List<InventoryWorkspaceTargetSlot> inputSlots,
            InventoryWorkspaceTargetSlot outputSlot,
            String diagnostics
    ) implements InventoryWorkspaceSurface {
        public CraftingSurface {
            id = id == null ? "" : id;
            toolId = toolId == null ? "" : toolId;
            inputSlots = inputSlots == null ? List.of() : List.copyOf(inputSlots.stream().filter(Objects::nonNull).toList());
            diagnostics = diagnostics == null ? "" : diagnostics;
        }

        @Override
        public InventoryWorkspaceSurfaceKind kind() {
            return InventoryWorkspaceSurfaceKind.CRAFTING;
        }
    }

    record WorkflowSurface(
            String id,
            InventoryWorkspaceSubjectRef.WorkflowRef subjectRef,
            CollectionProjection collections,
            RecentView recents,
            String selectedCollectionId,
            String selectedLoadoutId,
            String diagnostics
    ) implements InventoryWorkspaceSurface {
        public WorkflowSurface {
            id = id == null ? "" : id;
            subjectRef = subjectRef == null ? new InventoryWorkspaceSubjectRef.WorkflowRef(id) : subjectRef;
            collections = collections == null ? CollectionProjection.empty() : collections;
            recents = recents == null ? RecentView.empty() : recents;
            selectedCollectionId = selectedCollectionId == null ? "" : selectedCollectionId;
            selectedLoadoutId = selectedLoadoutId == null ? "" : selectedLoadoutId;
            diagnostics = diagnostics == null ? "" : diagnostics;
        }

        @Override
        public InventoryWorkspaceSurfaceKind kind() {
            return InventoryWorkspaceSurfaceKind.WORKFLOW;
        }
    }

    record StatusSurface(
            String id,
            InventoryWorkspaceSubjectRef.StatusRef subjectRef,
            InventoryWorkspaceStatus status,
            String diagnostics
    ) implements InventoryWorkspaceSurface {
        public StatusSurface {
            id = id == null ? "" : id;
            subjectRef = subjectRef == null ? new InventoryWorkspaceSubjectRef.StatusRef(id) : subjectRef;
            diagnostics = diagnostics == null ? "" : diagnostics;
        }

        @Override
        public InventoryWorkspaceSurfaceKind kind() {
            return InventoryWorkspaceSurfaceKind.STATUS;
        }
    }
}
