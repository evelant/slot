package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.action.InventoryActionKind;
import dev.imagio.slot.inventory.action.InventoryActionMode;
import dev.imagio.slot.inventory.action.InventoryActionOutcome;
import dev.imagio.slot.inventory.action.InventoryActionStatus;
import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.inventory.core.HostInstanceKey;
import dev.imagio.slot.inventory.core.InventoryActionPolicy;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.PortableContainerClassifiers;
import dev.imagio.slot.inventory.core.ServerMenuRef;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecentAndProtectionDomainTest {
    @Test
    void recentViewOrdersByMostRecentAcquisitionAndHonorsDismissalSequence() {
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        ItemIdentity stone = ItemIdentity.of("minecraft:stone");
        ItemIdentity dirt = ItemIdentity.of("minecraft:dirt");

        runtime.recordActivityEvent(activity(InventoryActivityProducer.WORLD_PICKUP, stone, 4));
        runtime.recordActivityEvent(activity(InventoryActivityProducer.WORLD_PICKUP, dirt, 2));
        runtime.recordActivityEvent(activity(InventoryActivityProducer.WORLD_PICKUP, stone, 1));

        assertEquals(java.util.List.of(stone, dirt), runtime.activityProjection().recents().visibleItems());
        assertEquals(1, runtime.activityProjection().recents().countsByIdentity().get(stone));

        assertTrue(runtime.dismissRecent(stone));
        assertEquals(java.util.List.of(dirt), runtime.activityProjection().recents().visibleItems());
    }

    @Test
    void protectionPolicyBlocksConfiguredIdentitiesAndTargets() {
        ItemIdentity shield = ItemIdentity.of("minecraft:shield");
        InventoryActionTarget target = new InventoryActionTarget.EquipmentTarget("equipment.offhand", 0);
        ProtectionSnapshotPolicy policy = new ProtectionSnapshotPolicy(Set.of(shield), Set.of(target), true);

        assertTrue(policy.protects(shield, InventoryActionKind.DROP));
        assertTrue(policy.protectsTarget(target, InventoryActionKind.UNEQUIP));
        assertTrue(policy.protectsPortableContainers());
        assertFalse(policy.protects(ItemIdentity.of("minecraft:stone"), InventoryActionKind.DROP));
    }

    @Test
    void runtimeConsumesOnlySuccessfulAuthoritativeOutcomes() {
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        ItemIdentity diamond = ItemIdentity.of("minecraft:diamond");

        assertTrue(runtime.recordOutcome(new InventoryActionOutcome(
                new HostInstanceKey("menu", 4, "test", ""),
                new ServerMenuRef("menu", 4),
                "req-1",
                InventoryActionKind.TRANSFER_STACK,
                InventoryActionMode.EXECUTE,
                "test",
                new InventoryActionTarget.SourceSlotTarget("player.main", 0),
                null,
                InventoryActionStatus.SUCCESS,
                java.util.List.of(),
                3,
                3,
                false,
                java.util.List.of(activity(InventoryActivityProducer.EXTERNAL_WITHDRAWAL, diamond, 3)),
                net.minecraft.world.item.ItemStack.EMPTY,
                ""
        )));
        assertFalse(runtime.recordOutcome(new InventoryActionOutcome(
                new HostInstanceKey("menu", 4, "test", ""),
                new ServerMenuRef("menu", 4),
                "req-2",
                InventoryActionKind.TRANSFER_STACK,
                InventoryActionMode.EXECUTE,
                "test",
                new InventoryActionTarget.SourceSlotTarget("player.main", 0),
                null,
                InventoryActionStatus.BLOCKED,
                java.util.List.of(),
                64,
                0,
                false,
                java.util.List.of(activity(InventoryActivityProducer.EXTERNAL_WITHDRAWAL, ItemIdentity.of("minecraft:stone"), 64)),
                net.minecraft.world.item.ItemStack.EMPTY,
                "failed"
        )));
        assertTrue(runtime.recordOutcome(new InventoryActionOutcome(
                new HostInstanceKey("menu", 4, "test", ""),
                new ServerMenuRef("menu", 4),
                "req-3",
                InventoryActionKind.TRANSFER_ONE,
                InventoryActionMode.EXECUTE,
                "test",
                new InventoryActionTarget.SourceSlotTarget("player.main", 1),
                null,
                InventoryActionStatus.SUCCESS,
                java.util.List.of(),
                1,
                1,
                false,
                java.util.List.of(activity(InventoryActivityProducer.TOOL_OUTPUT_EXTRACTION, ItemIdentity.of("minecraft:emerald"), 1)),
                net.minecraft.world.item.ItemStack.EMPTY,
                ""
        )));

        assertEquals(java.util.List.of(ItemIdentity.of("minecraft:emerald"), diamond), runtime.activityProjection().recents().visibleItems());
    }

    @Test
    void actionPolicyTreatsProtectedIdentitiesAndPortableContainersAsBlockedMutations() {
        ItemIdentity shield = ItemIdentity.of("minecraft:shield");
        ProtectionSnapshotPolicy policy = new ProtectionSnapshotPolicy(Set.of(shield), Set.of(), true);
        ItemStack container = new ItemStack("minecraft:shulker_box", 1, 1);
        PortableContainerClassifiers.register(stack -> {
            try {
                Object itemId = stack.getClass().getMethod("itemId").invoke(stack);
                return "minecraft:shulker_box".equals(itemId);
            } catch (ReflectiveOperationException ignored) {
                return false;
            }
        });

        assertTrue(InventoryActionPolicy.blockedByProtection(
                InventoryActionKind.DROP,
                shield,
                ItemStack.EMPTY,
                policy
        ));
        assertTrue(PortableContainerClassifiers.isPortableContainer(container));
        assertTrue(InventoryActionPolicy.blockedByProtection(
                InventoryActionKind.TRANSFER_STACK,
                null,
                container,
                policy
        ));
        assertFalse(InventoryActionPolicy.blockedByProtection(
                InventoryActionKind.USE,
                shield,
                container,
                policy
        ));
    }

    private static InventoryActivityEvent activity(InventoryActivityProducer producer, ItemIdentity identity, int count) {
        return new InventoryActivityEvent(
                producer == InventoryActivityProducer.TOOL_OUTPUT_EXTRACTION ? InventoryActivityKind.CRAFTED : InventoryActivityKind.ACQUIRED,
                producer,
                InventoryActivityConfidence.AUTHORITATIVE,
                identity,
                count,
                null,
                null,
                "",
                "",
                java.util.List.of(),
                ""
        );
    }
}
