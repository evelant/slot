package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.InventoryActionRoute;
import dev.imagio.slot.inventory.core.InventoryBindingRoute;
import dev.imagio.slot.inventory.core.InventoryCapability;
import dev.imagio.slot.inventory.core.HostInstanceKey;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventoryPaneMembership;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.InventorySourceDomain;
import dev.imagio.slot.inventory.core.InventorySourceRole;
import dev.imagio.slot.inventory.core.InventoryStackSnapshot;
import dev.imagio.slot.inventory.core.InventoryTopologyDescriptor;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.PlayerRuntimeStateDescriptor;
import dev.imagio.slot.inventory.integration.InventoryHostObservationHints;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.testsupport.InventoryAuthorityFixtures;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContextualSuggestionDomainServiceTest {
    @Test
    void sameStationContextDoesNotTreatPassiveCarriedItemsAsHints() {
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        ContextualSuggestionDomainService service = new ContextualSuggestionDomainService(repository, null);
        InventoryHostDescriptor host = hostWithToolSource();

        assertTrue(service.observeStationOpened(host, DomainEventMetadata.origin("test.station")));
        assertFalse(repository.contextualSuggestionState()
                .contextAggregates()
                .get("menu:" + TestMenu.class.getName())
                .itemHints()
                .containsKey("minecraft:wheat"));

        assertFalse(service.observeStationOpened(host, DomainEventMetadata.origin("test.station")));
        assertFalse(repository.contextualSuggestionState()
                .contextAggregates()
                .get("menu:" + TestMenu.class.getName())
                .itemHints()
                .containsKey("minecraft:oak_log"));
    }

    @Test
    void carriedOnlyHostDoesNotBecomeStationContext() {
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        ContextualSuggestionDomainService service = new ContextualSuggestionDomainService(repository, null);

        assertFalse(service.observeStationOpened(host(), DomainEventMetadata.origin("test.station")));
        assertTrue(repository.contextualSuggestionState().recentSignals().isEmpty());
        assertTrue(repository.contextualSuggestionState().activeContextKey().isBlank());
    }

    @Test
    void stationContentChangesRecordSignalsWithoutPassiveCarriedCooccurrence() {
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        ContextualSuggestionDomainService service = new ContextualSuggestionDomainService(repository, null);
        InventoryHostDescriptor host = hostWithToolSource();

        InventoryAuthoritySnapshot carriedKnife = authority(
                host,
                List.of(new InventoryStackSnapshot(0, new ItemStack("tfc:knife", 1, 1), 1)),
                List.of());
        service.observeStationContext(host, carriedKnife, 100L, DomainEventMetadata.origin("test.station"));

        InventoryAuthoritySnapshot leatherInserted = authority(
                host,
                List.of(new InventoryStackSnapshot(0, new ItemStack("tfc:knife", 1, 1), 1)),
                List.of(new InventoryStackSnapshot(0, new ItemStack("tfc:leather", 1, 64), 1)));
        assertTrue(service.observeStationContext(host, leatherInserted, 105L, DomainEventMetadata.origin("test.station")));

        assertTrue(repository.contextualSuggestionState().recentSignals().stream()
                .anyMatch(record -> record.event().kind() == ContextualSignalKind.STATION_CONTENTS_CHANGED
                        && ItemIdentity.of("tfc:leather").equals(record.event().identity())));
        assertFalse(repository.contextualSuggestionState()
                .associationIndex()
                .nextItemsBySignature()
                .values()
                .stream()
                .anyMatch(set -> set.itemHints().containsKey("tfc:knife")));
    }

    @Test
    void stationContentChangesLearnCooccurrenceBetweenMovedStationItems() {
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        ContextualSuggestionDomainService service = new ContextualSuggestionDomainService(repository, null);
        InventoryHostDescriptor host = hostWithToolSource();

        InventoryAuthoritySnapshot emptyStation = authority(host, List.of(), List.of());
        service.observeStationContext(host, emptyStation, 100L, DomainEventMetadata.origin("test.station"));

        InventoryAuthoritySnapshot leatherInserted = authority(
                host,
                List.of(),
                List.of(new InventoryStackSnapshot(0, new ItemStack("tfc:leather", 1, 64), 1)));
        assertTrue(service.observeStationContext(host, leatherInserted, 105L, DomainEventMetadata.origin("test.station")));

        InventoryAuthoritySnapshot sawInserted = authority(
                host,
                List.of(),
                List.of(
                        new InventoryStackSnapshot(0, new ItemStack("tfc:leather", 1, 64), 1),
                        new InventoryStackSnapshot(1, new ItemStack("slot:test_saw", 1, 1), 1)));
        assertTrue(service.observeStationContext(host, sawInserted, 110L, DomainEventMetadata.origin("test.station")));

        assertTrue(repository.contextualSuggestionState()
                .associationIndex()
                .nextItemsBySignature()
                .get(stationSignature("menu:" + TestMenu.class.getName(), "tfc:leather", "increase"))
                .itemHints()
                .containsKey("slot:test_saw"));
        assertTrue(repository.contextualSuggestionState()
                .associationIndex()
                .nextItemsBySignature()
                .get(stationSignature("menu:" + TestMenu.class.getName(), "slot:test_saw", "increase"))
                .itemHints()
                .containsKey("tfc:leather"));
    }

    @Test
    void stationContentDiffsIgnoreComponentOnlyChurn() {
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        ContextualSuggestionDomainService service = new ContextualSuggestionDomainService(repository, null);
        InventoryHostDescriptor host = hostWithToolSource();

        InventoryAuthoritySnapshot emptyStation = authority(host, List.of(), List.of());
        service.observeStationContext(host, emptyStation, 100L, DomainEventMetadata.origin("test.station"));

        InventoryAuthoritySnapshot warmingBrass = authority(
                host,
                List.of(),
                List.of(new InventoryStackSnapshot(
                        0,
                        new ItemStack("tfc:metal/ingot/brass", "{heat:100.0}", 1, 64),
                        1)));
        assertTrue(service.observeStationContext(host, warmingBrass, 105L, DomainEventMetadata.origin("test.station")));
        int signalCount = repository.contextualSuggestionState().recentSignals().size();

        InventoryAuthoritySnapshot hotterBrass = authority(
                host,
                List.of(),
                List.of(new InventoryStackSnapshot(
                        0,
                        new ItemStack("tfc:metal/ingot/brass", "{heat:725.0}", 1, 64),
                        1)));
        assertFalse(service.observeStationContext(host, hotterBrass, 110L, DomainEventMetadata.origin("test.station")));
        assertEquals(signalCount, repository.contextualSuggestionState().recentSignals().size());
    }

    @Test
    void authorityDiffAcquisitionsDoNotBecomeUsefulNowSignals() {
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        ContextualSuggestionDomainService service = new ContextualSuggestionDomainService(repository, null);

        boolean recorded = service.observeActivityRecord(activityRecord(
                InventoryActivityKind.ACQUIRED,
                InventoryActivityProducer.AUTHORITY_DIFF,
                "slot:test_knife"));

        assertFalse(recorded);
        assertTrue(repository.contextualSuggestionState().recentSignals().isEmpty());
    }

    @Test
    void worldPickupAcquisitionsStillBecomeUsefulNowSignals() {
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        ContextualSuggestionDomainService service = new ContextualSuggestionDomainService(repository, null);

        boolean recorded = service.observeActivityRecord(activityRecord(
                InventoryActivityKind.ACQUIRED,
                InventoryActivityProducer.WORLD_PICKUP,
                "minecraft:charcoal"));

        assertTrue(recorded);
        assertTrue(repository.contextualSuggestionState().recentSignals().stream()
                .anyMatch(record -> record.event().kind() == ContextualSignalKind.ITEM_ACQUIRED
                        && ItemIdentity.of("minecraft:charcoal").equals(record.event().identity())));
    }

    @Test
    void broadPersistedAssociationSignaturesArePruned() {
        ContextualAssociationSet polluted = ContextualAssociationSet.empty()
                .learn(ItemIdentity.of("firmalife:rennet"), 12L, 1L, 4.0D);
        String stationSignature = stationSignature("menu:" + TestMenu.class.getName(), "tfc:leather", "increase");

        ContextualAssociationIndex index = new ContextualAssociationIndex(Map.of(
                "station_opened|context=menu:net.minecraft.world.inventory.inventorymenu", polluted,
                "item_used|item=minecraft:oak_sapling|action=right_click_block|target=block:minecraft:dirt", polluted,
                stationSignature, polluted));

        assertFalse(index.nextItemsBySignature()
                .containsKey("station_opened|context=menu:net.minecraft.world.inventory.inventorymenu"));
        assertFalse(index.nextItemsBySignature()
                .containsKey("item_used|item=minecraft:oak_sapling|action=right_click_block|target=block:minecraft:dirt"));
        assertTrue(index.nextItemsBySignature().containsKey(stationSignature));
    }

    private static InventoryActivityRecord activityRecord(
            InventoryActivityKind kind,
            InventoryActivityProducer producer,
            String itemId
    ) {
        return new InventoryActivityRecord(
                new DomainEventEnvelope(1L, 1L, DomainEventStreamKind.ACTIVITY, 0L, "test", "", "", ""),
                new InventoryActivityEvent(
                        kind,
                        producer,
                        InventoryActivityConfidence.OBSERVED,
                        ItemIdentity.of(itemId),
                        1,
                        null,
                        null,
                        "",
                        "",
                        List.of(),
                        "test"));
    }

    private static String stationSignature(String contextKey, String itemId, String change) {
        return ContextualEventSignature.key(new ContextualSignalEvent(
                ContextualSignalKind.STATION_CONTENTS_CHANGED,
                ItemIdentity.of(itemId),
                1,
                0L,
                contextKey,
                "",
                "tool.input",
                Map.of("change", change)));
    }

    private static InventoryHostDescriptor host() {
        TestMenu menu = new TestMenu();
        return new InventoryHostDescriptor(
                new HostInstanceKey(TestMenu.class.getName(), 0, "slot.contextual.domain.test", ""),
                InventoryHostDescriptor.serverMenuRef(menu),
                "slot.contextual.domain.test",
                Component.literal("Campfire Pot"),
                menu,
                InventoryTopologyDescriptor.empty(),
                null,
                List.of(),
                PlayerRuntimeStateDescriptor.vanilla(0),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                InventoryHostObservationHints.defaults(),
                "");
    }

    private static InventoryHostDescriptor hostWithToolSource() {
        TestMenu menu = new TestMenu();
        InventorySourceDescriptor carried = InventorySourceDescriptor.builder("player.main")
                .label(Component.literal("Inventory"))
                .domain(InventorySourceDomain.PLAYER)
                .role(InventorySourceRole.MAIN)
                .logicalSlotCount(36)
                .bindingRoute(InventoryBindingRoute.PLAYER)
                .capabilities(Set.of(InventoryCapability.INSERT, InventoryCapability.EXTRACT))
                .actionRoute(InventoryActionRoute.PLAYER_MUTATION)
                .paneMembership(InventoryPaneMembership.CARRIED)
                .build();
        InventorySourceDescriptor toolInput = InventorySourceDescriptor.builder("tool.input")
                .label(Component.literal("Tool Input"))
                .domain(InventorySourceDomain.TOOL_REGION)
                .role(InventorySourceRole.PROVIDER_DEFINED)
                .logicalSlotCount(4)
                .bindingRoute(InventoryBindingRoute.MENU)
                .capabilities(Set.of(InventoryCapability.INSERT, InventoryCapability.EXTRACT))
                .actionRoute(InventoryActionRoute.MENU_MUTATION)
                .paneMembership(InventoryPaneMembership.HIDDEN)
                .build();
        return new InventoryHostDescriptor(
                new HostInstanceKey(TestMenu.class.getName(), 0, "slot.contextual.domain.test", ""),
                InventoryHostDescriptor.serverMenuRef(menu),
                "slot.contextual.domain.test",
                Component.literal("Leather Knapping"),
                menu,
                InventoryTopologyDescriptor.empty(),
                null,
                List.of(),
                PlayerRuntimeStateDescriptor.vanilla(0),
                List.of(carried, toolInput),
                List.of(),
                List.of(),
                List.of(new dev.imagio.slot.inventory.core.InventoryToolDescriptor(
                        "test.tool",
                        "test",
                        dev.imagio.slot.inventory.core.InventoryToolKind.CRAFTING_GRID,
                        Component.literal("Tool"),
                        null,
                        0,
                        false,
                        false,
                        false,
                        null,
                        List.of(),
                        List.of(),
                        List.of(),
                        Map.of(),
                        Map.of(),
                        null,
                        "")),
                InventoryHostObservationHints.defaults(),
                "");
    }

    private static InventoryAuthoritySnapshot authority(
            InventoryHostDescriptor host,
            List<InventoryStackSnapshot> carried,
            List<InventoryStackSnapshot> toolInput
    ) {
        return InventoryAuthorityFixtures.authority(
                host,
                Map.of(
                        "player.main", carried,
                        "tool.input", toolInput),
                Map.of(
                        "player.main", 36,
                        "tool.input", 4));
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
