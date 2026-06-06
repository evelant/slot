package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.action.InventoryActionKind;
import dev.imagio.slot.inventory.action.InventoryActionConflictPolicy;
import dev.imagio.slot.inventory.action.InventoryActionMode;
import dev.imagio.slot.inventory.action.InventoryActionOutcome;
import dev.imagio.slot.inventory.action.InventoryActionQuantity;
import dev.imagio.slot.inventory.action.InventoryActionScope;
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
    void recentViewKeepsOnlyDisplayCapacityMostRecentIdentities() {
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        for (int index = 0; index < RecentView.MAX_IDENTITIES + 5; index++) {
            runtime.recordActivityEvent(activity(
                    InventoryActivityProducer.WORLD_PICKUP,
                    ItemIdentity.of("mod:item_" + index),
                    1));
        }

        java.util.List<ItemIdentity> visible = runtime.activityProjection().recents().visibleItems();
        assertEquals(RecentView.MAX_IDENTITIES, visible.size());
        assertEquals(ItemIdentity.of("mod:item_" + (RecentView.MAX_IDENTITIES + 4)), visible.get(0));
        assertFalse(runtime.activityProjection().recents().countsByIdentity().containsKey(ItemIdentity.of("mod:item_0")));
    }

    @Test
    void protectionPolicyBlocksConfiguredIdentitiesAndTargets() {
        ItemIdentity shield = ItemIdentity.of("minecraft:shield");
        InventoryActionTarget target = new InventoryActionTarget.EquipmentTarget("equipment.offhand", 0);
        ProtectionSnapshotPolicy policy = new ProtectionSnapshotPolicy(Set.of(shield), Set.of(target), true);

        assertTrue(policy.protects(shield, InventoryActionKind.DROP_TO_WORLD));
        assertTrue(policy.protectsTarget(target, InventoryActionKind.TRANSFER));
        assertTrue(policy.protectsPortableContainers());
        assertFalse(policy.protects(ItemIdentity.of("minecraft:stone"), InventoryActionKind.DROP_TO_WORLD));
    }

    @Test
    void protectionPoliciesUseMovableIdentitySemantics() {
        ItemIdentity hammer = ItemIdentity.of("gtceu:steel_mining_hammer");
        ItemIdentity damagedHammer = ItemIdentity.exact("gtceu:steel_mining_hammer", "{Damage:512}");
        ItemIdentity toolStateHammer = ItemIdentity.exact(
                "gtceu:steel_mining_hammer",
                "{Damage:12,\"GT.Tool\":{MaxDamage:960}}");

        ProtectionSnapshotPolicy snapshot = new ProtectionSnapshotPolicy(Set.of(damagedHammer), Set.of(), false);
        CarryTargetProtection carry = new CarryTargetProtection(ProtectionPolicy.allowAll(), Set.of(damagedHammer));
        KitActiveProtection activeKit = new KitActiveProtection(ProtectionPolicy.allowAll(), Set.of(damagedHammer));

        assertTrue(snapshot.protects(toolStateHammer, InventoryActionKind.DROP_TO_WORLD));
        assertTrue(carry.protects(hammer, InventoryActionKind.DROP_TO_WORLD));
        assertTrue(activeKit.protects(toolStateHammer, InventoryActionKind.DROP_TO_WORLD));
    }

    @Test
    void recentsUseMovableIdentitySemanticsForDismissalAndStorage() {
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        ItemIdentity hammer = ItemIdentity.of("gtceu:steel_mining_hammer");
        ItemIdentity damagedHammer = ItemIdentity.exact("gtceu:steel_mining_hammer", "{Damage:512}");
        ItemIdentity toolStateHammer = ItemIdentity.exact(
                "gtceu:steel_mining_hammer",
                "{Damage:12,\"GT.Tool\":{MaxDamage:960}}");

        runtime.recordActivityEvent(activity(InventoryActivityProducer.WORLD_PICKUP, damagedHammer, 1));
        runtime.recordActivityEvent(activity(InventoryActivityProducer.WORLD_PICKUP, toolStateHammer, 1));

        assertEquals(java.util.List.of(hammer), runtime.activityProjection().recents().visibleItems());
        assertTrue(runtime.dismissRecent(damagedHammer));
        assertTrue(runtime.activityProjection().recents().visibleItems().isEmpty());
    }

    @Test
    void runtimeConsumesOnlySuccessfulAuthoritativeOutcomes() {
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        ItemIdentity diamond = ItemIdentity.of("minecraft:diamond");

        assertTrue(runtime.recordOutcome(new InventoryActionOutcome(
                new HostInstanceKey("menu", 4, "test", ""),
                new ServerMenuRef("menu", 4),
                "req-1",
                InventoryActionKind.TRANSFER,
                InventoryActionMode.EXECUTE,
                InventoryActionQuantity.STACK,
                InventoryActionScope.SINGLE_TARGET,
                InventoryActionConflictPolicy.INSERT_ONLY,
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
                InventoryActionKind.TRANSFER,
                InventoryActionMode.EXECUTE,
                InventoryActionQuantity.STACK,
                InventoryActionScope.SINGLE_TARGET,
                InventoryActionConflictPolicy.INSERT_ONLY,
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
                InventoryActionKind.TRANSFER,
                InventoryActionMode.EXECUTE,
                InventoryActionQuantity.ONE,
                InventoryActionScope.SINGLE_TARGET,
                InventoryActionConflictPolicy.INSERT_ONLY,
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
                InventoryActionKind.DROP_TO_WORLD,
                shield,
                ItemStack.EMPTY,
                policy
        ));
        assertTrue(PortableContainerClassifiers.isPortableContainer(container));
        assertTrue(InventoryActionPolicy.blockedByProtection(
                InventoryActionKind.TRANSFER,
                null,
                container,
                policy
        ));
        assertTrue(InventoryActionPolicy.blockedByProtection(
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
