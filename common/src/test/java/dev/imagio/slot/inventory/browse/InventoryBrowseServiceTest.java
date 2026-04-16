package dev.imagio.slot.inventory.browse;

import dev.imagio.slot.inventory.action.InventoryActionScope;
import dev.imagio.slot.inventory.action.InventoryCommandId;
import dev.imagio.slot.inventory.action.InventoryCommandReasonCode;
import dev.imagio.slot.inventory.core.BuiltinInventoryDescriptors;
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
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
import dev.imagio.slot.inventory.core.InventoryTopologyDescriptor;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.PlayerRuntimeStateDescriptor;
import dev.imagio.slot.inventory.integration.InventoryHostSession;
import dev.imagio.slot.testsupport.InventoryAuthorityFixtures;
import dev.imagio.slot.workflow.domain.CollectionDefinition;
import dev.imagio.slot.workflow.domain.InMemoryWorkflowDomainStateRepository;
import dev.imagio.slot.workflow.domain.InventoryActivityConfidence;
import dev.imagio.slot.workflow.domain.InventoryActivityEvent;
import dev.imagio.slot.workflow.domain.InventoryActivityKind;
import dev.imagio.slot.workflow.domain.InventoryActivityProducer;
import dev.imagio.slot.workflow.domain.LoadoutTarget;
import dev.imagio.slot.workflow.domain.QuickAccessLoadoutEntry;
import dev.imagio.slot.workflow.domain.QuickAccessLoadoutDefinition;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryBrowseServiceTest {
    @Test
    void browseServiceBuildsMergedItemsPlaceholdersAndLoadoutsFromCoreState() {
        InventoryHostDescriptor host = host();
        var authority = InventoryAuthorityFixtures.authority(
                host,
                Map.of(
                        BuiltinInventoryIds.PLAYER_MAIN, List.of(new InventoryStackSnapshot(0, new ItemStack("minecraft:torch", 3, 64), 3)),
                        "carried.backpack.one", List.of(new InventoryStackSnapshot(0, new ItemStack("minecraft:torch", 2, 64), 2)),
                        "carried.backpack.two", List.of(new InventoryStackSnapshot(0, new ItemStack("minecraft:torch", 5, 64), 5)),
                        "external.chest", List.of(new InventoryStackSnapshot(0, new ItemStack("minecraft:torch", 64, 64), 64))
                ),
                Map.of(
                        BuiltinInventoryIds.PLAYER_MAIN, 27,
                        "carried.backpack.one", 9,
                        "carried.backpack.two", 9,
                        "external.chest", 9
                )
        );

        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(repository, null);
        CollectionDefinition mining = runtime.collectionWorkflow().createCollection("Mining");
        runtime.collectionWorkflow().toggleCollectionMembership(ItemIdentity.of("minecraft:torch"), mining.id());
        runtime.collectionWorkflow().toggleCollectionMembership(ItemIdentity.of("minecraft:pickaxe"), mining.id());
        runtime.collectionWorkflow().setDesiredCount(mining.id(), ItemIdentity.of("minecraft:torch"), 16);
        runtime.collectionWorkflow().setDesiredCount(mining.id(), ItemIdentity.of("minecraft:pickaxe"), 1);
        runtime.collectionWorkflow().toggleFavorite(ItemIdentity.of("minecraft:torch"));
        QuickAccessLoadoutDefinition miningKit = runtime.collectionWorkflow().createLoadout(
                mining.id(),
                "Mining Kit",
                Set.of(new QuickAccessLoadoutEntry(
                        new LoadoutTarget.QuickAccessLaneTarget(BuiltinInventoryIds.QUICK_ACCESS_LANE_0, 0),
                        ItemIdentity.of("minecraft:torch")
                ))
        );
        runtime.recordActivityEvent(new InventoryActivityEvent(
                InventoryActivityKind.ACQUIRED,
                InventoryActivityProducer.WORLD_PICKUP,
                InventoryActivityConfidence.OBSERVED,
                ItemIdentity.of("minecraft:torch"),
                4,
                null,
                null,
                "",
                "",
                List.of(),
                ""
        ));
        repository.browseSessionState().replaceWith(new InventoryBrowseSessionState(
                new InventoryBrowseFilter("", InventoryBrowseFilterScope.SELECTED_COLLECTION),
                InventoryBrowseSortMode.COUNT_DESC,
                InventoryBrowseGroupingMode.FLAT,
                InventoryBrowsePaneMode.DUAL_PANE,
                InventoryPaneMembership.CARRIED,
                mining.id(),
                miningKit.id(),
                "",
                InventoryActionScope.VISIBLE_MATCHES,
                null,
                Set.of()
        ));

        InventoryBrowseDocument document = InventoryBrowseService.browse(new InventoryBrowseRequest(
                authority,
                repository.snapshot(),
                repository.browsePreferences().current(),
                repository.browseSessionState().current(),
                entry -> ItemIdentity.of(entry.stack().itemId())
        ));

        assertEquals(InventoryBrowsePaneMode.DUAL_PANE, document.paneMode());
        assertEquals(2, document.panes().size());

        InventoryBrowsePane carriedPane = pane(document, InventoryPaneMembership.CARRIED);
        InventoryBrowseEntry.ItemEntry torchEntry = itemEntry(carriedPane, "minecraft:torch");
        InventoryBrowseEntry.PlaceholderEntry pickaxePlaceholder = placeholderEntry(carriedPane, "minecraft:pickaxe");
        InventoryBrowseEntry.LoadoutEntry loadoutEntry = loadoutEntry(carriedPane, "Mining Kit");

        assertEquals(10, torchEntry.row().visibleTotalCount());
        assertEquals(3, torchEntry.row().backingEntries().size());
        assertTrue(torchEntry.annotations().favorite());
        assertTrue(torchEntry.annotations().recent());
        assertEquals(16, torchEntry.annotations().desiredCount());
        assertTrue(torchEntry.commands().get(InventoryCommandId.TRANSFER_ALL_EXACT).available());
        assertTrue(torchEntry.commands().get(InventoryCommandId.DISMISS_RECENT).available());

        assertTrue(pickaxePlaceholder.commands().get(InventoryCommandId.TRANSFER_STACK).reasonCodes()
                .contains(InventoryCommandReasonCode.PLACEHOLDER_ONLY));
        assertTrue(pickaxePlaceholder.commands().get(InventoryCommandId.TOGGLE_COLLECTION_MEMBERSHIP).available());

        assertTrue(loadoutEntry.selected());
        assertTrue(loadoutEntry.commands().get(InventoryCommandId.APPLY_LOADOUT).available());
        assertTrue(loadoutEntry.commands().get(InventoryCommandId.SELECT_LOADOUT).reasonCodes()
                .contains(InventoryCommandReasonCode.ALREADY_SELECTED));

        InventoryBrowsePane externalPane = pane(document, InventoryPaneMembership.EXTERNAL);
        InventoryBrowseEntry.ItemEntry externalTorchEntry = itemEntry(externalPane, "minecraft:torch");
        assertEquals(64, externalTorchEntry.row().visibleTotalCount());
    }

    @Test
    void browseDocumentAndTypedCommandsAreDeterministicAcrossEquivalentRequests() {
        InventoryHostDescriptor host = host();
        var authority = InventoryAuthorityFixtures.authority(
                host,
                Map.of(
                        BuiltinInventoryIds.PLAYER_MAIN, List.of(new InventoryStackSnapshot(0, new ItemStack("minecraft:stone", 12, 64), 12)),
                        "carried.backpack.one", List.of(new InventoryStackSnapshot(1, new ItemStack("minecraft:stone", 8, 64), 8))
                ),
                Map.of(
                        BuiltinInventoryIds.PLAYER_MAIN, 27,
                        "carried.backpack.one", 9
                )
        );

        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(repository, null);
        CollectionDefinition building = runtime.collectionWorkflow().createCollection("Building");
        runtime.collectionWorkflow().toggleCollectionMembership(ItemIdentity.of("minecraft:stone"), building.id());
        repository.browseSessionState().replaceWith(new InventoryBrowseSessionState(
                new InventoryBrowseFilter("stone", InventoryBrowseFilterScope.SELECTED_COLLECTION),
                InventoryBrowseSortMode.NAME,
                InventoryBrowseGroupingMode.FLAT,
                InventoryBrowsePaneMode.CARRIED_ONLY,
                InventoryPaneMembership.CARRIED,
                building.id(),
                "",
                "",
                InventoryActionScope.VISIBLE_MATCHES,
                null,
                Set.of()
        ));

        InventoryBrowseRequest request = new InventoryBrowseRequest(
                authority,
                repository.snapshot(),
                repository.browsePreferences().current(),
                repository.browseSessionState().current(),
                entry -> ItemIdentity.of(entry.stack().itemId())
        );

        InventoryBrowseDocument first = InventoryBrowseService.browse(request);
        InventoryBrowseDocument second = InventoryBrowseService.browse(request);

        assertEquals(contract(first), contract(second));
    }

    private static InventoryHostDescriptor host() {
        TestMenu menu = new TestMenu();
        return new InventoryHostDescriptor(
                new HostInstanceKey(TestMenu.class.getName(), 0, "browse.test", ""),
                InventoryHostDescriptor.serverMenuRef(menu),
                "browse.test",
                Component.literal("Browse Test"),
                menu,
                InventoryTopologyDescriptor.empty(),
                InventoryHostSession.empty(),
                List.of(),
                PlayerRuntimeStateDescriptor.vanilla(0),
                List.of(
                        BuiltinInventoryDescriptors.playerMain(InventoryTopologyDescriptor.empty()),
                        BuiltinInventoryDescriptors.quickAccessLane0Source(InventoryTopologyDescriptor.empty()),
                        BuiltinInventoryDescriptors.armorSource(InventoryTopologyDescriptor.empty()),
                        BuiltinInventoryDescriptors.offhandSource(InventoryTopologyDescriptor.empty()),
                        carriedSource("carried.backpack.one", 40),
                        carriedSource("carried.backpack.two", 50),
                        externalSource("external.chest", 100)
                ),
                BuiltinInventoryDescriptors.builtInQuickAccessLanes(),
                BuiltinInventoryDescriptors.builtInEquipmentGroups(),
                List.of(),
                dev.imagio.slot.inventory.integration.InventoryHostObservationHints.defaults(),
                ""
        );
    }

    private static InventoryBrowsePane pane(InventoryBrowseDocument document, InventoryPaneMembership paneMembership) {
        return document.panes().stream()
                .filter(pane -> pane.paneMembership() == paneMembership)
                .findFirst()
                .orElseThrow();
    }

    private static InventoryBrowseEntry.ItemEntry itemEntry(InventoryBrowsePane pane, String itemId) {
        return flattenEntries(pane).stream()
                .filter(InventoryBrowseEntry.ItemEntry.class::isInstance)
                .map(InventoryBrowseEntry.ItemEntry.class::cast)
                .filter(entry -> entry.row().identity() != null && itemId.equals(entry.row().identity().itemId()))
                .findFirst()
                .orElseThrow();
    }

    private static InventoryBrowseEntry.PlaceholderEntry placeholderEntry(InventoryBrowsePane pane, String itemId) {
        return flattenEntries(pane).stream()
                .filter(InventoryBrowseEntry.PlaceholderEntry.class::isInstance)
                .map(InventoryBrowseEntry.PlaceholderEntry.class::cast)
                .filter(entry -> entry.identity() != null && itemId.equals(entry.identity().itemId()))
                .findFirst()
                .orElseThrow();
    }

    private static InventoryBrowseEntry.LoadoutEntry loadoutEntry(InventoryBrowsePane pane, String loadoutName) {
        return flattenEntries(pane).stream()
                .filter(InventoryBrowseEntry.LoadoutEntry.class::isInstance)
                .map(InventoryBrowseEntry.LoadoutEntry.class::cast)
                .filter(entry -> entry.loadout() != null && loadoutName.equals(entry.loadout().name()))
                .findFirst()
                .orElseThrow();
    }

    private static List<String> contract(InventoryBrowseDocument document) {
        ArrayList<String> lines = new ArrayList<>();
        for (InventoryBrowsePane pane : document.panes()) {
            for (InventoryBrowseSection section : pane.sections()) {
                for (InventoryBrowseEntry entry : section.entries()) {
                    lines.add(entry.subjectRef().stableKey() + "|" + entry.commands());
                }
            }
        }
        return List.copyOf(lines);
    }

    private static List<InventoryBrowseEntry> flattenEntries(InventoryBrowsePane pane) {
        ArrayList<InventoryBrowseEntry> entries = new ArrayList<>();
        for (InventoryBrowseSection section : pane.sections()) {
            entries.addAll(section.entries());
        }
        return List.copyOf(entries);
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
                .logicalSlotCount(9)
                .bindingRoute(InventoryBindingRoute.PROVIDER)
                .capabilities(Set.of(InventoryCapability.INSERT, InventoryCapability.EXTRACT))
                .actionRoute(InventoryActionRoute.PROVIDER_MUTATION)
                .paneMembership(InventoryPaneMembership.EXTERNAL)
                .stableOrder(stableOrder)
                .build();
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
