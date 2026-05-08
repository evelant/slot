package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.inventory.browse.InventoryBrowseDocument;
import dev.imagio.slot.inventory.browse.InventoryBrowseDocumentQueries;
import dev.imagio.slot.inventory.browse.InventoryBrowsePane;
import dev.imagio.slot.inventory.browse.InventoryBrowseSubjectRef;
import dev.imagio.slot.inventory.core.CraftingSurfaceDescriptor;
import dev.imagio.slot.inventory.core.EquipmentGroupDescriptor;
import dev.imagio.slot.inventory.core.HostInstanceKey;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventoryPaneMembership;
import dev.imagio.slot.inventory.core.InventoryToolDescriptor;
import dev.imagio.slot.inventory.core.QuickAccessLaneDescriptor;
import dev.imagio.slot.inventory.core.ToolRegionDescriptor;
import dev.imagio.slot.inventory.session.InventorySessionSnapshot;
import dev.imagio.slot.workflow.domain.WorkflowDomainSnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

public final class InventoryWorkspaceComposer {
    private InventoryWorkspaceComposer() {
    }

    public static InventoryWorkspaceModel compose(InventorySessionSnapshot snapshot) {
        if (snapshot == null) {
            return InventoryWorkspaceModel.empty(null, null);
        }
        InventoryHostDescriptor host = snapshot.host();
        HostInstanceKey hostId = host == null ? HostInstanceKey.empty() : host.hostId();
        if (host == null) {
            return InventoryWorkspaceModel.empty(snapshot.token(), hostId);
        }

        InventoryWorkspaceProfile profile = InventoryWorkspaceProfiles.select(snapshot);
        LinkedHashMap<InventoryWorkspaceZoneKind, InventoryWorkspaceZone> zonesByKind = new LinkedHashMap<>();
        zonesByKind.putAll(browseZones(snapshot.browseDocument(), profile));

        InventoryWorkspaceZone quickAccessZone = quickAccessZone(host);
        if (quickAccessZone != null) {
            zonesByKind.put(InventoryWorkspaceZoneKind.QUICK_ACCESS, quickAccessZone);
        }

        InventoryWorkspaceZone equipmentZone = equipmentZone(host);
        if (equipmentZone != null) {
            zonesByKind.put(InventoryWorkspaceZoneKind.EQUIPMENT, equipmentZone);
        }

        InventoryWorkspaceZone workflowZone = workflowZone(snapshot.workflow());
        if (workflowZone != null) {
            zonesByKind.put(InventoryWorkspaceZoneKind.WORKFLOW_RAIL, workflowZone);
        }

        InventoryWorkspaceZone toolDockZone = toolDockZone(snapshot);
        if (toolDockZone != null) {
            zonesByKind.put(InventoryWorkspaceZoneKind.TOOL_DOCK, toolDockZone);
        }

        InventoryWorkspaceStatus status = status(snapshot, host, toolDockZone);
        zonesByKind.put(
                InventoryWorkspaceZoneKind.STATUS_RAIL,
                new InventoryWorkspaceZone(
                        "status_rail",
                        InventoryWorkspaceZoneKind.STATUS_RAIL,
                        false,
                        List.of(new InventoryWorkspaceSurface.StatusSurface(
                                "status",
                                new InventoryWorkspaceSubjectRef.StatusRef("status"),
                                status,
                                ""
                        )),
                        ""
                )
        );

        ArrayList<InventoryWorkspaceZone> orderedZones = new ArrayList<>();
        for (InventoryWorkspaceZoneKind kind : profile.zoneOrder()) {
            InventoryWorkspaceZone zone = zonesByKind.get(kind);
            if (zone != null && (!zone.surfaces().isEmpty() || kind == InventoryWorkspaceZoneKind.STATUS_RAIL)) {
                orderedZones.add(zone);
            }
        }

        return new InventoryWorkspaceModel(
                snapshot.token(),
                hostId,
                profile,
                List.copyOf(orderedZones),
                defaultFocusSubject(snapshot.browseDocument(), orderedZones),
                status,
                ""
        );
    }

    private static LinkedHashMap<InventoryWorkspaceZoneKind, InventoryWorkspaceZone> browseZones(
            InventoryBrowseDocument browseDocument,
            InventoryWorkspaceProfile profile
    ) {
        LinkedHashMap<InventoryWorkspaceZoneKind, InventoryWorkspaceZone> zones = new LinkedHashMap<>();
        if (browseDocument == null || browseDocument.panes().isEmpty()) {
            return zones;
        }
        List<InventoryWorkspaceSurface.BrowsePaneSurface> browseSurfaces = orderedBrowseSurfaces(browseDocument, profile.id());
        if (!browseSurfaces.isEmpty()) {
            zones.put(
                    InventoryWorkspaceZoneKind.PRIMARY_BROWSE,
                    new InventoryWorkspaceZone(
                            "primary_browse",
                            InventoryWorkspaceZoneKind.PRIMARY_BROWSE,
                            true,
                            List.of(browseSurfaces.get(0)),
                            ""
                    )
            );
        }
        if (browseSurfaces.size() > 1) {
            zones.put(
                    InventoryWorkspaceZoneKind.SECONDARY_BROWSE,
                    new InventoryWorkspaceZone(
                            "secondary_browse",
                            InventoryWorkspaceZoneKind.SECONDARY_BROWSE,
                            false,
                            List.of(browseSurfaces.get(1)),
                            ""
                    )
            );
        }
        return zones;
    }

    private static List<InventoryWorkspaceSurface.BrowsePaneSurface> orderedBrowseSurfaces(
            InventoryBrowseDocument browseDocument,
            InventoryWorkspaceProfileId profileId
    ) {
        InventoryBrowsePane carriedPane = pane(browseDocument, InventoryPaneMembership.CARRIED);
        InventoryBrowsePane externalPane = pane(browseDocument, InventoryPaneMembership.EXTERNAL);
        ArrayList<InventoryWorkspaceSurface.BrowsePaneSurface> ordered = new ArrayList<>();
        switch (profileId) {
            case CARRIED -> {
                InventoryBrowsePane primary = carriedPane != null ? carriedPane : externalPane;
                if (primary != null) {
                    ordered.add(browseSurface(primary, true));
                }
            }
            case DUAL_PANE -> {
                InventoryBrowsePane primary = browseDocument.activePane() == InventoryPaneMembership.EXTERNAL && externalPane != null
                        ? externalPane
                        : carriedPane != null ? carriedPane : externalPane;
                InventoryBrowsePane secondary = primary == carriedPane ? externalPane : carriedPane;
                if (primary != null) {
                    ordered.add(browseSurface(primary, true));
                }
                if (secondary != null) {
                    ordered.add(browseSurface(secondary, false));
                }
            }
            case TERMINAL_HYBRID -> {
                InventoryBrowsePane primary = externalPane != null ? externalPane : carriedPane;
                InventoryBrowsePane secondary = primary == externalPane ? carriedPane : null;
                if (primary != null) {
                    ordered.add(browseSurface(primary, true));
                }
                if (secondary != null) {
                    ordered.add(browseSurface(secondary, false));
                }
            }
        }
        return List.copyOf(ordered);
    }

    private static InventoryWorkspaceSurface.BrowsePaneSurface browseSurface(InventoryBrowsePane pane, boolean primary) {
        return new InventoryWorkspaceSurface.BrowsePaneSurface(
                "browse:" + pane.paneMembership().name().toLowerCase(),
                pane.paneMembership(),
                primary,
                new InventoryWorkspaceSubjectRef.BrowseRef(pane.subjectRef()),
                pane,
                pane.diagnostics()
        );
    }

    private static InventoryBrowsePane pane(InventoryBrowseDocument browseDocument, InventoryPaneMembership paneMembership) {
        if (browseDocument == null || paneMembership == null) {
            return null;
        }
        return browseDocument.panes().stream()
                .filter(pane -> pane != null && pane.paneMembership() == paneMembership)
                .findFirst()
                .orElse(null);
    }

    private static InventoryWorkspaceZone quickAccessZone(InventoryHostDescriptor host) {
        if (host == null || host.quickAccessLanes().isEmpty()) {
            return null;
        }
        ArrayList<InventoryWorkspaceSurface.QuickAccessLaneSurface> lanes = new ArrayList<>();
        host.quickAccessLanes().stream()
                .sorted(Comparator.comparingInt(QuickAccessLaneDescriptor::stableOrder).thenComparing(QuickAccessLaneDescriptor::id))
                .forEach(lane -> {
                    ArrayList<InventoryWorkspaceTargetSlot> slots = new ArrayList<>();
                    for (int slotIndex = 0; slotIndex < lane.logicalSlotCount(); slotIndex++) {
                        boolean selected = lane.id().equals(host.playerRuntimeState().selectedQuickAccessLaneId())
                                && slotIndex == host.playerRuntimeState().selectedQuickAccessSlotIndex();
                        slots.add(new InventoryWorkspaceTargetSlot(
                                "quick_access:" + lane.id() + "#" + slotIndex,
                                new InventoryActionTarget.QuickAccessTarget(lane.id(), slotIndex),
                                new InventoryWorkspaceSubjectRef.QuickAccessSlotRef(lane.id(), slotIndex),
                                slotIndex,
                                selected,
                                selected,
                                ""
                        ));
                    }
                    lanes.add(new InventoryWorkspaceSurface.QuickAccessLaneSurface(
                            lane,
                            new InventoryWorkspaceSubjectRef.QuickAccessLaneRef(lane.id()),
                            List.copyOf(slots)
                    ));
                });
        return new InventoryWorkspaceZone(
                "quick_access",
                InventoryWorkspaceZoneKind.QUICK_ACCESS,
                false,
                List.of(new InventoryWorkspaceSurface.QuickAccessSurface(
                        "quick_access",
                        List.copyOf(lanes),
                        host.playerRuntimeState().selectedQuickAccessLaneId(),
                        host.playerRuntimeState().selectedQuickAccessSlotIndex(),
                        ""
                )),
                ""
        );
    }

    private static InventoryWorkspaceZone equipmentZone(InventoryHostDescriptor host) {
        if (host == null || host.equipmentGroups().isEmpty()) {
            return null;
        }
        ArrayList<InventoryWorkspaceSurface.EquipmentGroupSurface> groups = new ArrayList<>();
        host.equipmentGroups().stream()
                .sorted(Comparator.comparingInt(EquipmentGroupDescriptor::stableOrder).thenComparing(EquipmentGroupDescriptor::id))
                .forEach(group -> {
                    ArrayList<InventoryWorkspaceTargetSlot> slots = new ArrayList<>();
                    for (int slotIndex = 0; slotIndex < group.logicalSlotCount(); slotIndex++) {
                        slots.add(new InventoryWorkspaceTargetSlot(
                                "equipment:" + group.id() + "#" + slotIndex,
                                new InventoryActionTarget.EquipmentTarget(group.id(), slotIndex),
                                new InventoryWorkspaceSubjectRef.EquipmentSlotRef(group.id(), slotIndex),
                                slotIndex,
                                false,
                                false,
                                ""
                        ));
                    }
                    groups.add(new InventoryWorkspaceSurface.EquipmentGroupSurface(
                            group,
                            new InventoryWorkspaceSubjectRef.EquipmentGroupRef(group.id()),
                            List.copyOf(slots)
                    ));
                });
        return new InventoryWorkspaceZone(
                "equipment",
                InventoryWorkspaceZoneKind.EQUIPMENT,
                false,
                List.of(new InventoryWorkspaceSurface.EquipmentSurface("equipment", List.copyOf(groups), "")),
                ""
        );
    }

    private static InventoryWorkspaceZone workflowZone(WorkflowDomainSnapshot workflow) {
        if (workflow == null) {
            return null;
        }
        return new InventoryWorkspaceZone(
                "workflow_rail",
                InventoryWorkspaceZoneKind.WORKFLOW_RAIL,
                false,
                List.of(new InventoryWorkspaceSurface.WorkflowSurface(
                        "workflow",
                        new InventoryWorkspaceSubjectRef.WorkflowRef("workflow"),
                        workflow.collections(),
                        workflow.recents(),
                        workflow.browseSessionState().selectedCollectionId(),
                        workflow.browseSessionState().selectedLoadoutId(),
                        ""
                )),
                ""
        );
    }

    private static InventoryWorkspaceZone toolDockZone(InventorySessionSnapshot snapshot) {
        InventoryHostDescriptor host = snapshot == null ? null : snapshot.host();
        if (host == null || host.toolDescriptors().isEmpty()) {
            return null;
        }
        ArrayList<InventoryWorkspaceSurface> surfaces = new ArrayList<>();
        String pinnedToolId = snapshot.workflow().browseSessionState().pinnedToolId();
        host.toolDescriptors().stream()
                .sorted(Comparator.comparingInt(InventoryToolDescriptor::priority).reversed().thenComparing(InventoryToolDescriptor::id))
                .forEach(tool -> {
                    ArrayList<InventoryWorkspaceSurface.ToolRegionSurface> regions = new ArrayList<>();
                    for (ToolRegionDescriptor region : tool.regions()) {
                        if (region == null) {
                            continue;
                        }
                        ArrayList<InventoryWorkspaceTargetSlot> slots = new ArrayList<>();
                        for (int slotIndex = 0; slotIndex < region.logicalSlotCount(); slotIndex++) {
                            slots.add(new InventoryWorkspaceTargetSlot(
                                    "tool_region:" + tool.id() + "|" + region.id() + "#" + slotIndex,
                                    new InventoryActionTarget.ToolRegionTarget(tool.id(), region.id(), slotIndex),
                                    new InventoryWorkspaceSubjectRef.ToolRegionSlotRef(tool.id(), region.id(), slotIndex),
                                    slotIndex,
                                    false,
                                    false,
                                    ""
                            ));
                        }
                        regions.add(new InventoryWorkspaceSurface.ToolRegionSurface(region, List.copyOf(slots)));
                    }

                    surfaces.add(new InventoryWorkspaceSurface.ToolSurface(
                            "tool:" + tool.id(),
                            tool,
                            new InventoryWorkspaceSubjectRef.ToolRef(tool.id()),
                            tool.id().equals(pinnedToolId),
                            List.copyOf(regions),
                            tool.diagnostics()
                    ));

                    CraftingSurfaceDescriptor craftingSurface = tool.craftingSurface();
                    if (craftingSurface != null && craftingSurface.present()) {
                        ArrayList<InventoryWorkspaceTargetSlot> inputSlots = new ArrayList<>();
                        for (int inputIndex = 0; inputIndex < craftingSurface.inputSlotCount(); inputIndex++) {
                            inputSlots.add(new InventoryWorkspaceTargetSlot(
                                    "crafting:" + tool.id() + "/input#" + inputIndex,
                                    craftingSurface.inputSlotTarget(inputIndex),
                                    new InventoryWorkspaceSubjectRef.CraftingInputRef(tool.id(), inputIndex),
                                    inputIndex,
                                    false,
                                    false,
                                    ""
                            ));
                        }
                        InventoryWorkspaceTargetSlot outputSlot = new InventoryWorkspaceTargetSlot(
                                "crafting:" + tool.id() + "/output",
                                craftingSurface.outputSlotTarget(),
                                new InventoryWorkspaceSubjectRef.CraftingOutputRef(tool.id()),
                                0,
                                false,
                                false,
                                ""
                        );
                        surfaces.add(new InventoryWorkspaceSurface.CraftingSurface(
                                "crafting:" + tool.id(),
                                tool.id(),
                                craftingSurface,
                                List.copyOf(inputSlots),
                                outputSlot,
                                craftingSurface.diagnostics()
                        ));
                    }
                });

        return new InventoryWorkspaceZone(
                "tool_dock",
                InventoryWorkspaceZoneKind.TOOL_DOCK,
                false,
                List.copyOf(surfaces),
                ""
        );
    }

    private static InventoryWorkspaceStatus status(
            InventorySessionSnapshot snapshot,
            InventoryHostDescriptor host,
            InventoryWorkspaceZone toolDockZone
    ) {
        ArrayList<String> diagnostics = new ArrayList<>();
        addDiagnostic(diagnostics, snapshot.diagnostics());
        addDiagnostic(diagnostics, snapshot.browseDocument().diagnostics());
        addDiagnostic(diagnostics, host.diagnostics());
        boolean craftingPresent = toolDockZone != null && toolDockZone.surfaces().stream()
                .anyMatch(surface -> surface.kind() == InventoryWorkspaceSurfaceKind.CRAFTING);
        return new InventoryWorkspaceStatus(
                host.observationHints().hostFamilyHint(),
                host.observationHints().carriedOnly(),
                snapshot.pendingActions().size(),
                toolDockZone != null && !toolDockZone.surfaces().isEmpty(),
                craftingPresent,
                List.copyOf(diagnostics)
        );
    }

    private static void addDiagnostic(List<String> diagnostics, String value) {
        if (value != null && !value.isBlank()) {
            diagnostics.add(value);
        }
    }

    private static InventoryWorkspaceSubjectRef defaultFocusSubject(
            InventoryBrowseDocument browseDocument,
            List<InventoryWorkspaceZone> orderedZones
    ) {
        InventoryBrowseSubjectRef selectedBrowseSubject = browseDocument == null ? null : browseDocument.sessionState().selectedSubject();
        if (selectedBrowseSubject != null && InventoryBrowseDocumentQueries.containsSubject(browseDocument, selectedBrowseSubject)) {
            return new InventoryWorkspaceSubjectRef.BrowseRef(selectedBrowseSubject);
        }
        for (InventoryWorkspaceZone zone : orderedZones) {
            if (zone.kind() == InventoryWorkspaceZoneKind.PRIMARY_BROWSE) {
                InventoryWorkspaceSurface.BrowsePaneSurface browseSurface = zone.surfaces().stream()
                        .filter(InventoryWorkspaceSurface.BrowsePaneSurface.class::isInstance)
                        .map(InventoryWorkspaceSurface.BrowsePaneSurface.class::cast)
                        .findFirst()
                        .orElse(null);
                if (browseSurface != null) {
                    return browseSurface.surfaceRef();
                }
            }
        }
        for (InventoryWorkspaceZone zone : orderedZones) {
            for (InventoryWorkspaceSurface surface : zone.surfaces()) {
                if (surface instanceof InventoryWorkspaceSurface.QuickAccessSurface quickAccessSurface && !quickAccessSurface.lanes().isEmpty()) {
                    return quickAccessSurface.lanes().get(0).subjectRef();
                }
                if (surface instanceof InventoryWorkspaceSurface.EquipmentSurface equipmentSurface && !equipmentSurface.groups().isEmpty()) {
                    return equipmentSurface.groups().get(0).subjectRef();
                }
                if (surface instanceof InventoryWorkspaceSurface.ToolSurface toolSurface) {
                    return toolSurface.subjectRef();
                }
                if (surface instanceof InventoryWorkspaceSurface.WorkflowSurface workflowSurface) {
                    return workflowSurface.subjectRef();
                }
                if (surface instanceof InventoryWorkspaceSurface.StatusSurface statusSurface) {
                    return statusSurface.subjectRef();
                }
            }
        }
        return null;
    }
}
