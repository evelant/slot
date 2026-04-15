package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.action.InventoryActionScope;
import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.inventory.browse.HeuristicInventoryCategoryResolver;
import dev.imagio.slot.inventory.browse.InventoryBrowseDocument;
import dev.imagio.slot.inventory.browse.InventoryBrowseFilter;
import dev.imagio.slot.inventory.browse.InventoryBrowseFilterScope;
import dev.imagio.slot.inventory.browse.InventoryBrowseGroupingMode;
import dev.imagio.slot.inventory.browse.InventoryBrowsePaneMode;
import dev.imagio.slot.inventory.browse.InventoryBrowseRequest;
import dev.imagio.slot.inventory.browse.InventoryBrowseSessionState;
import dev.imagio.slot.inventory.browse.InventoryBrowseSortMode;
import dev.imagio.slot.inventory.browse.InventoryBrowseSubjectRef;
import dev.imagio.slot.inventory.browse.InventoryBrowseService;
import dev.imagio.slot.inventory.browse.InventoryCategoryOverrides;
import dev.imagio.slot.inventory.core.BuiltinInventoryDescriptors;
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.CraftingSurfaceDescriptor;
import dev.imagio.slot.inventory.core.HostInstanceKey;
import dev.imagio.slot.inventory.core.InventoryActionRoute;
import dev.imagio.slot.inventory.core.InventoryBindingRoute;
import dev.imagio.slot.inventory.core.InventoryCapability;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventoryPaneMembership;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.InventorySourceDomain;
import dev.imagio.slot.inventory.core.InventorySourceRole;
import dev.imagio.slot.inventory.core.InventoryStackSnapshot;
import dev.imagio.slot.inventory.core.InventoryToolAction;
import dev.imagio.slot.inventory.core.InventoryToolActionId;
import dev.imagio.slot.inventory.core.InventoryToolDescriptor;
import dev.imagio.slot.inventory.core.InventoryToolKind;
import dev.imagio.slot.inventory.core.InventoryTopologyDescriptor;
import dev.imagio.slot.inventory.core.PlayerRuntimeStateDescriptor;
import dev.imagio.slot.inventory.core.ToolPresentationHints;
import dev.imagio.slot.inventory.core.ToolRegionDescriptor;
import dev.imagio.slot.inventory.core.ToolRegionRole;
import dev.imagio.slot.inventory.integration.InventoryHostFamilyHint;
import dev.imagio.slot.inventory.integration.InventoryHostObservationHints;
import dev.imagio.slot.inventory.integration.InventoryHostSession;
import dev.imagio.slot.inventory.integration.InventorySlotOwnershipPosture;
import dev.imagio.slot.inventory.session.InventorySessionSnapshot;
import dev.imagio.slot.inventory.session.InventorySessionToken;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.testsupport.InventoryAuthorityFixtures;
import dev.imagio.slot.workflow.domain.InMemoryWorkflowDomainStateRepository;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class InventoryWorkspaceComposerTest {
    @Test
    void dualPaneWorkspaceUsesActivePaneAsPrimaryAndPreservesBrowseSelection() {
        InventoryBrowseSubjectRef selected = new InventoryBrowseSubjectRef.ItemRowRef(
                InventoryPaneMembership.EXTERNAL,
                ItemIdentity.of("minecraft:torch")
        );
        InventorySessionSnapshot snapshot = snapshot(
                host(
                        new InventoryHostObservationHints(
                                InventoryHostFamilyHint.DUAL_PANE,
                                InventorySlotOwnershipPosture.SLOT_OWNED,
                                false,
                                true,
                                Map.of()
                        ),
                        List.of(
                                carriedSource("carried.backpack", 30),
                                externalSource("external.chest", 100)
                        ),
                        List.of()
                ),
                new InventoryBrowseSessionState(
                        new InventoryBrowseFilter("", InventoryBrowseFilterScope.ALL),
                        InventoryBrowseSortMode.NAME,
                        InventoryBrowseGroupingMode.FLAT,
                        InventoryBrowsePaneMode.DUAL_PANE,
                        InventoryPaneMembership.EXTERNAL,
                        "",
                        "",
                        "",
                        InventoryActionScope.VISIBLE_MATCHES,
                        selected,
                        Set.of()
                ),
                Map.of(
                        BuiltinInventoryIds.PLAYER_MAIN, List.of(new InventoryStackSnapshot(0, new ItemStack("minecraft:bread", 2, 64), 2)),
                        "carried.backpack", List.of(new InventoryStackSnapshot(0, new ItemStack("minecraft:torch", 3, 64), 3)),
                        "external.chest", List.of(new InventoryStackSnapshot(0, new ItemStack("minecraft:torch", 64, 64), 64))
                )
        );

        InventoryWorkspaceModel workspace = snapshot.workspaceModel();
        assertEquals(InventoryWorkspaceProfileId.DUAL_PANE, workspace.profileId());
        assertEquals(
                new InventoryWorkspaceSubjectRef.BrowseRef(selected).stableKey(),
                workspace.defaultFocusSubject().stableKey()
        );

        InventoryWorkspaceSurface.BrowsePaneSurface primary = browseSurface(workspace.zone(InventoryWorkspaceZoneKind.PRIMARY_BROWSE));
        InventoryWorkspaceSurface.BrowsePaneSurface secondary = browseSurface(workspace.zone(InventoryWorkspaceZoneKind.SECONDARY_BROWSE));
        assertEquals(InventoryPaneMembership.EXTERNAL, primary.paneMembership());
        assertEquals(InventoryPaneMembership.CARRIED, secondary.paneMembership());
    }

    @Test
    void carriedWorkspaceRetainsCarriedProfileWhileExposingToolAndCraftingSurfaces() {
        InventoryHostDescriptor host = host(
                new InventoryHostObservationHints(
                        InventoryHostFamilyHint.CARRIED_ONLY,
                        InventorySlotOwnershipPosture.HYBRID,
                        true,
                        true,
                        Map.of()
                ),
                List.of(
                        carriedSource("carried.backpack", 30),
                        hiddenSource("tool.craft/input.source", 200, 4),
                        hiddenSource("tool.craft/output.source", 201, 1)
                ),
                List.of(craftingTool("tool.craft", "tool.craft/input.source", "tool.craft/output.source", 2))
        );

        InventorySessionSnapshot snapshot = snapshot(
                host,
                new InventoryBrowseSessionState(
                        new InventoryBrowseFilter("", InventoryBrowseFilterScope.ALL),
                        InventoryBrowseSortMode.NAME,
                        InventoryBrowseGroupingMode.FLAT,
                        InventoryBrowsePaneMode.CARRIED_ONLY,
                        InventoryPaneMembership.CARRIED,
                        "",
                        "",
                        "tool.craft",
                        InventoryActionScope.VISIBLE_MATCHES,
                        null,
                        Set.of()
                ),
                Map.of(
                        BuiltinInventoryIds.PLAYER_MAIN, List.of(new InventoryStackSnapshot(0, new ItemStack("minecraft:apple", 1, 64), 1)),
                        "carried.backpack", List.of(new InventoryStackSnapshot(0, new ItemStack("minecraft:stick", 8, 64), 8))
                )
        );

        InventoryWorkspaceModel workspace = snapshot.workspaceModel();
        assertEquals(InventoryWorkspaceProfileId.CARRIED, workspace.profileId());
        assertNull(workspace.zone(InventoryWorkspaceZoneKind.SECONDARY_BROWSE));
        assertNotNull(workspace.zone(InventoryWorkspaceZoneKind.QUICK_ACCESS));
        assertNotNull(workspace.zone(InventoryWorkspaceZoneKind.EQUIPMENT));
        assertNotNull(workspace.zone(InventoryWorkspaceZoneKind.WORKFLOW_RAIL));

        InventoryWorkspaceZone toolDock = workspace.zone(InventoryWorkspaceZoneKind.TOOL_DOCK);
        assertNotNull(toolDock);
        assertEquals(
                List.of(InventoryWorkspaceSurfaceKind.TOOL, InventoryWorkspaceSurfaceKind.CRAFTING),
                toolDock.surfaces().stream().map(InventoryWorkspaceSurface::kind).toList()
        );
    }

    @Test
    void terminalWorkspacePlacesToolDockAheadOfSecondaryBrowse() {
        InventorySessionSnapshot snapshot = snapshot(
                host(
                        new InventoryHostObservationHints(
                                InventoryHostFamilyHint.TERMINAL_HYBRID,
                                InventorySlotOwnershipPosture.HYBRID,
                                false,
                                true,
                                Map.of()
                        ),
                        List.of(
                                carriedSource("carried.backpack", 30),
                                externalSource("terminal.primary", 100),
                                hiddenSource("terminal.craft/input.source", 200, 9),
                                hiddenSource("terminal.craft/output.source", 201, 1)
                        ),
                        List.of(craftingTool("terminal.craft", "terminal.craft/input.source", "terminal.craft/output.source", 3))
                ),
                new InventoryBrowseSessionState(
                        new InventoryBrowseFilter("", InventoryBrowseFilterScope.ALL),
                        InventoryBrowseSortMode.NAME,
                        InventoryBrowseGroupingMode.FLAT,
                        InventoryBrowsePaneMode.DUAL_PANE,
                        InventoryPaneMembership.EXTERNAL,
                        "",
                        "",
                        "terminal.craft",
                        InventoryActionScope.VISIBLE_MATCHES,
                        null,
                        Set.of()
                ),
                Map.of(
                        BuiltinInventoryIds.PLAYER_MAIN, List.of(new InventoryStackSnapshot(0, new ItemStack("minecraft:iron_ingot", 5, 64), 5)),
                        "carried.backpack", List.of(new InventoryStackSnapshot(0, new ItemStack("minecraft:coal", 3, 64), 3)),
                        "terminal.primary", List.of(new InventoryStackSnapshot(0, new ItemStack("minecraft:glass", 32, 64), 32))
                )
        );

        InventoryWorkspaceModel workspace = snapshot.workspaceModel();
        assertEquals(InventoryWorkspaceProfileId.TERMINAL_HYBRID, workspace.profileId());
        assertEquals(
                List.of(
                        InventoryWorkspaceZoneKind.PRIMARY_BROWSE,
                        InventoryWorkspaceZoneKind.TOOL_DOCK,
                        InventoryWorkspaceZoneKind.SECONDARY_BROWSE
                ),
                workspace.zones().subList(0, 3).stream().map(InventoryWorkspaceZone::kind).toList()
        );
        assertEquals(
                InventoryPaneMembership.EXTERNAL,
                browseSurface(workspace.zone(InventoryWorkspaceZoneKind.PRIMARY_BROWSE)).paneMembership()
        );
        assertEquals(
                InventoryPaneMembership.CARRIED,
                browseSurface(workspace.zone(InventoryWorkspaceZoneKind.SECONDARY_BROWSE)).paneMembership()
        );
    }

    @Test
    void workspaceCompositionIsDeterministicForEquivalentSnapshots() {
        InventoryHostDescriptor host = host(
                new InventoryHostObservationHints(
                        InventoryHostFamilyHint.DUAL_PANE,
                        InventorySlotOwnershipPosture.SLOT_OWNED,
                        false,
                        true,
                        Map.of()
                ),
                List.of(carriedSource("carried.backpack", 30), externalSource("external.chest", 100)),
                List.of()
        );
        InventoryBrowseSessionState browseState = new InventoryBrowseSessionState(
                new InventoryBrowseFilter("torch", InventoryBrowseFilterScope.ALL),
                InventoryBrowseSortMode.NAME,
                InventoryBrowseGroupingMode.FLAT,
                InventoryBrowsePaneMode.DUAL_PANE,
                InventoryPaneMembership.CARRIED,
                "",
                "",
                "",
                InventoryActionScope.VISIBLE_MATCHES,
                null,
                Set.of()
        );
        Map<String, List<InventoryStackSnapshot>> sourceSnapshots = Map.of(
                BuiltinInventoryIds.PLAYER_MAIN, List.of(new InventoryStackSnapshot(0, new ItemStack("minecraft:torch", 2, 64), 2)),
                "external.chest", List.of(new InventoryStackSnapshot(0, new ItemStack("minecraft:torch", 64, 64), 64))
        );

        InventoryWorkspaceModel first = snapshot(host, browseState, sourceSnapshots).workspaceModel();
        InventoryWorkspaceModel second = snapshot(host, browseState, sourceSnapshots).workspaceModel();

        assertEquals(contract(first), contract(second));
    }

    private static InventorySessionSnapshot snapshot(
            InventoryHostDescriptor host,
            InventoryBrowseSessionState browseState,
            Map<String, List<InventoryStackSnapshot>> sourceSnapshots
    ) {
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        repository.browseSessionState().replaceWith(browseState);
        var authority = InventoryAuthorityFixtures.authority(host, sourceSnapshots, Map.of());
        InventoryBrowseDocument browseDocument = InventoryBrowseService.browse(new InventoryBrowseRequest(
                authority,
                repository.snapshot(),
                repository.browsePreferences().current(),
                repository.browseSessionState().current(),
                entry -> ItemIdentity.of(entry.stack().itemId()),
                new HeuristicInventoryCategoryResolver(InventoryCategoryOverrides.empty())
        ));
        return InventorySessionSnapshot.create(
                new InventorySessionToken("workspace-test", 1L),
                host,
                authority,
                repository.snapshot(),
                browseDocument,
                List.of(),
                ""
        );
    }

    private static InventoryHostDescriptor host(
            InventoryHostObservationHints observationHints,
            List<InventorySourceDescriptor> additionalSources,
            List<InventoryToolDescriptor> tools
    ) {
        TestMenu menu = new TestMenu();
        ArrayList<InventorySourceDescriptor> sources = new ArrayList<>();
        sources.add(BuiltinInventoryDescriptors.playerMain(InventoryTopologyDescriptor.empty()));
        sources.add(BuiltinInventoryDescriptors.quickAccessLane0Source(InventoryTopologyDescriptor.empty()));
        sources.add(BuiltinInventoryDescriptors.armorSource(InventoryTopologyDescriptor.empty()));
        sources.add(BuiltinInventoryDescriptors.offhandSource(InventoryTopologyDescriptor.empty()));
        sources.addAll(additionalSources);
        return new InventoryHostDescriptor(
                new HostInstanceKey(TestMenu.class.getName(), additionalSources.size(), "workspace.test", ""),
                InventoryHostDescriptor.serverMenuRef(menu),
                "workspace.test",
                Component.literal("Workspace Test"),
                menu,
                InventoryTopologyDescriptor.empty(),
                InventoryHostSession.empty(),
                List.of(),
                PlayerRuntimeStateDescriptor.vanilla(0),
                List.copyOf(sources),
                BuiltinInventoryDescriptors.builtInQuickAccessLanes(),
                BuiltinInventoryDescriptors.builtInEquipmentGroups(),
                List.copyOf(tools),
                observationHints,
                ""
        );
    }

    private static InventorySourceDescriptor carriedSource(String sourceId, int stableOrder) {
        return InventorySourceDescriptor.builder(sourceId)
                .label(Component.literal(sourceId))
                .domain(InventorySourceDomain.PLAYER_EXTENSION)
                .role(InventorySourceRole.PROVIDER_DEFINED)
                .logicalSlotCount(9)
                .bindingRoute(InventoryBindingRoute.PROVIDER)
                .capabilities(Set.of(InventoryCapability.INSERT, InventoryCapability.EXTRACT))
                .actionRoute(InventoryActionRoute.PROVIDER_MUTATION)
                .paneMembership(InventoryPaneMembership.CARRIED)
                .stableOrder(stableOrder)
                .build();
    }

    private static InventorySourceDescriptor externalSource(String sourceId, int stableOrder) {
        return InventorySourceDescriptor.builder(sourceId)
                .label(Component.literal(sourceId))
                .domain(InventorySourceDomain.HOST_STORAGE)
                .role(InventorySourceRole.PRIMARY_STORAGE)
                .logicalSlotCount(27)
                .bindingRoute(InventoryBindingRoute.PROVIDER)
                .capabilities(Set.of(InventoryCapability.INSERT, InventoryCapability.EXTRACT))
                .actionRoute(InventoryActionRoute.PROVIDER_MUTATION)
                .paneMembership(InventoryPaneMembership.EXTERNAL)
                .stableOrder(stableOrder)
                .build();
    }

    private static InventorySourceDescriptor hiddenSource(String sourceId, int stableOrder, int slotCount) {
        return InventorySourceDescriptor.builder(sourceId)
                .label(Component.literal(sourceId))
                .domain(InventorySourceDomain.HOST_STORAGE)
                .role(InventorySourceRole.PROVIDER_DEFINED)
                .logicalSlotCount(slotCount)
                .bindingRoute(InventoryBindingRoute.MENU)
                .capabilities(Set.of(InventoryCapability.INSERT, InventoryCapability.EXTRACT))
                .actionRoute(InventoryActionRoute.MENU_MUTATION)
                .paneMembership(InventoryPaneMembership.HIDDEN)
                .stableOrder(stableOrder)
                .build();
    }

    private static InventoryToolDescriptor craftingTool(String toolId, String inputSourceId, String outputSourceId, int gridWidth) {
        int inputCount = gridWidth * gridWidth;
        return new InventoryToolDescriptor(
                toolId,
                "workspace.test",
                InventoryToolKind.CRAFTING_GRID,
                Component.literal("Crafting"),
                new ToolPresentationHints("Crafting", 0, "docked", 70),
                70,
                true,
                true,
                true,
                null,
                List.of(
                        new ToolRegionDescriptor(
                                toolId + "/input",
                                ToolRegionRole.INPUT,
                                inputCount,
                                InventoryBindingRoute.MENU,
                                Set.of(InventoryCapability.TOOL_REGION_MUTATION, InventoryCapability.INSERT, InventoryCapability.EXTRACT),
                                false,
                                "",
                                ""
                        ),
                        new ToolRegionDescriptor(
                                toolId + "/output",
                                ToolRegionRole.OUTPUT,
                                1,
                                InventoryBindingRoute.MENU,
                                Set.of(InventoryCapability.TOOL_REGION_MUTATION, InventoryCapability.EXTRACT),
                                false,
                                "",
                                ""
                        )
                ),
                List.of(new InventoryToolAction(
                        "clear_grid",
                        InventoryToolActionId.CLEAR_GRID,
                        Component.literal("Clear"),
                        Component.empty()
                )),
                List.of(),
                Map.of(),
                Map.of(),
                new CraftingSurfaceDescriptor(
                        java.util.stream.IntStream.range(0, inputCount)
                                .mapToObj(index -> new InventoryActionTarget.SourceSlotTarget(inputSourceId, index))
                                .toList(),
                        new InventoryActionTarget.SourceSlotTarget(outputSourceId, 0),
                        gridWidth,
                        gridWidth,
                        true,
                        true,
                        true,
                        gridWidth == 3,
                        ""
                ),
                ""
        );
    }

    private static InventoryWorkspaceSurface.BrowsePaneSurface browseSurface(InventoryWorkspaceZone zone) {
        assertNotNull(zone);
        return zone.surfaces().stream()
                .filter(InventoryWorkspaceSurface.BrowsePaneSurface.class::isInstance)
                .map(InventoryWorkspaceSurface.BrowsePaneSurface.class::cast)
                .findFirst()
                .orElseThrow();
    }

    private static String contract(InventoryWorkspaceModel workspace) {
        return workspace.profileId()
                + "|"
                + (workspace.defaultFocusSubject() == null ? "" : workspace.defaultFocusSubject().stableKey())
                + "|"
                + workspace.zones().stream()
                .map(zone -> zone.kind() + ":" + zone.surfaces().stream().map(surface -> surface.kind() + ":" + surface.id()).toList())
                .toList();
    }

    private static final class TestMenu extends AbstractContainerMenu {
        private TestMenu() {
            super(null, 0);
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }
    }
}
