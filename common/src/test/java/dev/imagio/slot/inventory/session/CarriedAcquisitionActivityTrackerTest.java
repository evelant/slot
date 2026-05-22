package dev.imagio.slot.inventory.session;

import dev.imagio.slot.inventory.core.HostInstanceKey;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventoryPaneMembership;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.InventorySourceDomain;
import dev.imagio.slot.inventory.core.InventorySourceRole;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.core.ServerMenuRef;
import dev.imagio.slot.inventory.query.CursorStateSnapshot;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntryKey;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.inventory.query.InventorySourceSnapshot;
import dev.imagio.slot.workflow.domain.InMemoryWorkflowDomainStateRepository;
import dev.imagio.slot.workflow.domain.InventoryActivityConfidence;
import dev.imagio.slot.workflow.domain.InventoryActivityEvent;
import dev.imagio.slot.workflow.domain.InventoryActivityKind;
import dev.imagio.slot.workflow.domain.InventoryActivityProducer;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CarriedAcquisitionActivityTrackerTest {
    @Test
    void pendingExplicitPickupSuppressionDoesNotSwallowUnrelatedAcquisition() {
        CarriedAcquisitionActivityTracker tracker = new CarriedAcquisitionActivityTracker();
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        String key = "player-1";
        ItemIdentity stone = ItemIdentity.of("minecraft:stone");
        ItemIdentity diamond = ItemIdentity.of("minecraft:diamond");

        tracker.observe(key, authority(Map.of()), runtime, CarriedAcquisitionActivityTrackerTest::identity, "seed");
        runtime.recordActivityEvent(activity(stone, 32));
        tracker.suppressAcquired(key, stone, 32);

        int recorded = tracker.observe(
                key,
                authority(Map.of("minecraft:stone", 32, "minecraft:diamond", 1)),
                runtime,
                CarriedAcquisitionActivityTrackerTest::identity,
                "menu_slot_changed");

        assertEquals(1, recorded);
        assertEquals(List.of(diamond, stone), runtime.activityProjection().recents().visibleItems());
        assertEquals(1, runtime.activityProjection().recents().countsByIdentity().get(diamond));
        assertEquals(32, runtime.activityProjection().recents().countsByIdentity().get(stone));
    }

    @Test
    void pendingExplicitPickupSuppressionRecordsOnlyUnsuppressedRemainder() {
        CarriedAcquisitionActivityTracker tracker = new CarriedAcquisitionActivityTracker();
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        String key = "player-1";
        ItemIdentity stone = ItemIdentity.of("minecraft:stone");

        tracker.observe(key, authority(Map.of()), runtime, CarriedAcquisitionActivityTrackerTest::identity, "seed");
        runtime.recordActivityEvent(activity(stone, 10));
        tracker.suppressAcquired(key, stone, 10);

        int recorded = tracker.observe(
                key,
                authority(Map.of("minecraft:stone", 15)),
                runtime,
                CarriedAcquisitionActivityTrackerTest::identity,
                "menu_slot_changed");

        assertEquals(1, recorded);
        assertEquals(List.of(stone), runtime.activityProjection().recents().visibleItems());
        assertEquals(5, runtime.activityProjection().recents().countsByIdentity().get(stone));
    }

    @Test
    void cursorPickupFromExternalSlotCountsAsCarriedAcquisition() {
        CarriedAcquisitionActivityTracker tracker = new CarriedAcquisitionActivityTracker();
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        String key = "player-1";
        ItemIdentity mold = ItemIdentity.of("tfc:ceramic_mold");

        tracker.observe(key, authority(Map.of()), runtime, CarriedAcquisitionActivityTrackerTest::identity, "seed");

        int recorded = tracker.observe(
                key,
                authority(Map.of(), new ItemStack("tfc:ceramic_mold", 1, 1)),
                runtime,
                CarriedAcquisitionActivityTrackerTest::identity,
                "menu_slot_changed");

        assertEquals(1, recorded);
        assertEquals(List.of(mold), runtime.activityProjection().recents().visibleItems());
        assertEquals(1, runtime.activityProjection().recents().countsByIdentity().get(mold));
    }

    @Test
    void cursorPickupFromPlayerSlotDoesNotCountAsNewAcquisition() {
        CarriedAcquisitionActivityTracker tracker = new CarriedAcquisitionActivityTracker();
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        String key = "player-1";

        tracker.observe(
                key,
                authority(Map.of("minecraft:stone", 4)),
                runtime,
                CarriedAcquisitionActivityTrackerTest::identity,
                "seed");

        int recorded = tracker.observe(
                key,
                authority(Map.of("minecraft:stone", 3), new ItemStack("minecraft:stone", 1, 64)),
                runtime,
                CarriedAcquisitionActivityTrackerTest::identity,
                "menu_slot_changed");

        assertEquals(0, recorded);
        assertEquals(List.of(), runtime.activityProjection().recents().visibleItems());
    }

    @Test
    void unchangedObserveClearsPendingSuppressionBeforeLaterVanillaPickup() {
        CarriedAcquisitionActivityTracker tracker = new CarriedAcquisitionActivityTracker();
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        String key = "player-1";
        ItemIdentity stone = ItemIdentity.of("minecraft:stone");

        tracker.observe(key, authority(Map.of()), runtime, CarriedAcquisitionActivityTrackerTest::identity, "seed");
        runtime.recordActivityEvent(activity(stone, 4));
        tracker.suppressAcquired(key, stone, 4);

        assertEquals(0, tracker.observe(
                key,
                authority(Map.of()),
                runtime,
                CarriedAcquisitionActivityTrackerTest::identity,
                "suppress_acquired"));

        assertEquals(1, tracker.observe(
                key,
                authority(Map.of("minecraft:stone", 1)),
                runtime,
                CarriedAcquisitionActivityTrackerTest::identity,
                "menu_slot_changed"));
        assertEquals(1, runtime.activityProjection().recents().countsByIdentity().get(stone));
    }

    private static InventoryActivityEvent activity(ItemIdentity identity, int count) {
        return new InventoryActivityEvent(
                InventoryActivityKind.ACQUIRED,
                InventoryActivityProducer.WORLD_PICKUP,
                InventoryActivityConfidence.AUTHORITATIVE,
                identity,
                count,
                null,
                null,
                "",
                "",
                List.of(),
                "test");
    }

    private static ItemIdentity identity(InventoryEntrySnapshot entry) {
        return entry == null || !entry.present() ? null : ItemIdentityMatcher.create(entry.stack());
    }

    private static InventoryAuthoritySnapshot authority(Map<String, Integer> countsByItemId) {
        return authority(countsByItemId, ItemStack.EMPTY);
    }

    private static InventoryAuthoritySnapshot authority(Map<String, Integer> countsByItemId, ItemStack cursorStack) {
        InventorySourceDescriptor source = InventorySourceDescriptor.builder("player.main")
                .domain(InventorySourceDomain.PLAYER)
                .role(InventorySourceRole.MAIN)
                .paneMembership(InventoryPaneMembership.CARRIED)
                .logicalSlotCount(27)
                .build();
        InventoryHostDescriptor host = new InventoryHostDescriptor(
                new HostInstanceKey("test-menu", 1, "test", ""),
                new ServerMenuRef("test-menu", 1),
                "test-menu",
                Component.literal("Test"),
                new TestMenu(),
                null,
                null,
                List.of(),
                null,
                List.of(source),
                List.of(),
                List.of(),
                List.of(),
                null,
                "");
        ArrayList<InventoryEntrySnapshot> entries = new ArrayList<>();
        int slot = 0;
        for (Map.Entry<String, Integer> entry : new LinkedHashMap<>(countsByItemId).entrySet()) {
            entries.add(new InventoryEntrySnapshot(
                    InventoryEntryKey.slot("player.main", slot++),
                    new ItemStack(entry.getKey(), entry.getValue(), 64),
                    entry.getValue(),
                    ""));
        }
        return new InventoryAuthoritySnapshot(
                host,
                Map.of("player.main", new InventorySourceSnapshot("player.main", 27, entries, "")),
                new CursorStateSnapshot(cursorStack, ""));
    }

    private static final class TestMenu extends AbstractContainerMenu {
        private TestMenu() {
            super(null, 1);
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }
    }
}
