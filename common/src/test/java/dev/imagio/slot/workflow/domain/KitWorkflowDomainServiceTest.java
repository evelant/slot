package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KitWorkflowDomainServiceTest {
    @Test
    void createAssignsSlugIdAndStoresKit() {
        KitWorkflowDomainService kits = kits();

        KitDefinition first = kits.create("Mining");

        assertEquals("mining", first.id());
        assertEquals("Mining", first.name());
        assertEquals(1, first.pageCount());
        assertEquals(List.of(first), kits.kits());
    }

    @Test
    void createRejectsBlankName() {
        KitWorkflowDomainService kits = kits();
        assertThrows(IllegalArgumentException.class, () -> kits.create(""));
        assertThrows(IllegalArgumentException.class, () -> kits.create("   "));
    }

    @Test
    void createDeduplicatesSlugOnCollision() {
        KitWorkflowDomainService kits = kits();
        kits.create("Mining");
        KitDefinition second = kits.create("Mining");

        assertEquals("mining-2", second.id());
    }

    @Test
    void renameUpdatesOnlyTheName() {
        KitWorkflowDomainService kits = kits();
        KitDefinition kit = kits.create("Mining");

        assertTrue(kits.rename(kit.id(), "Quarry"));
        assertEquals("Quarry", kits.kit(kit.id()).name());
        assertEquals(kit.id(), kits.kit(kit.id()).id());
    }

    @Test
    void renameReturnsFalseWhenUnchanged() {
        KitWorkflowDomainService kits = kits();
        KitDefinition kit = kits.create("Mining");
        assertFalse(kits.rename(kit.id(), "Mining"));
    }

    @Test
    void updateReplacesPagesAndBring() {
        KitWorkflowDomainService kits = kits();
        KitDefinition kit = kits.create("Combat");
        ItemIdentity sword = ItemIdentity.of("minecraft:iron_sword");

        KitPage page = KitPage.empty().withSlot(0, sword);
        KitDefinition next = kit.withPages(List.of(page)).withBring(List.of(ItemIdentity.of("minecraft:apple")));

        assertTrue(kits.update(next));
        KitDefinition stored = kits.kit(kit.id());
        assertEquals(sword, stored.page(0).slot(0));
        assertEquals(List.of(ItemIdentity.of("minecraft:apple")), stored.bring());
    }

    @Test
    void deleteRemovesKitAndClearsActivationIfMatching() {
        KitWorkflowDomainService kits = kits();
        KitDefinition kit = kits.create("Mining");
        kits.activate(kit.id());
        assertTrue(kits.activation().isActive());

        kits.delete(kit.id());

        assertNull(kits.kit(kit.id()));
        assertFalse(kits.activation().isActive());
    }

    @Test
    void activateSetsActivation() {
        KitWorkflowDomainService kits = kits();
        KitDefinition kit = kits.create("Mining");

        assertTrue(kits.activate(kit.id()));
        assertEquals(kit.id(), kits.activation().kitId());
        assertEquals(0, kits.activation().pageIndex());
    }

    @Test
    void activateReturnsFalseWhenAlreadyActive() {
        KitWorkflowDomainService kits = kits();
        KitDefinition kit = kits.create("Mining");
        kits.activate(kit.id());

        assertFalse(kits.activate(kit.id()));
    }

    @Test
    void deactivateClearsActivation() {
        KitWorkflowDomainService kits = kits();
        KitDefinition kit = kits.create("Mining");
        kits.activate(kit.id());

        assertTrue(kits.deactivate());
        assertFalse(kits.activation().isActive());
    }

    @Test
    void deactivateReturnsFalseWhenNoneActive() {
        KitWorkflowDomainService kits = kits();
        assertFalse(kits.deactivate());
    }

    @Test
    void switchPageAdvancesThroughMultiPageKit() {
        KitWorkflowDomainService kits = kits();
        KitPage pageA = KitPage.empty().withSlot(0, ItemIdentity.of("minecraft:pickaxe"));
        KitPage pageB = KitPage.empty().withSlot(0, ItemIdentity.of("minecraft:sword"));
        KitPage pageC = KitPage.empty().withSlot(0, ItemIdentity.of("minecraft:fishing_rod"));
        KitDefinition kit = kits.create("Multi");
        kits.update(kit.withPages(List.of(pageA, pageB, pageC)));
        kits.activate(kit.id());

        assertTrue(kits.switchPage(1));
        assertEquals(1, kits.activation().pageIndex());
        assertTrue(kits.switchPage(2));
        assertEquals(2, kits.activation().pageIndex());
        assertTrue(kits.switchPage(0));
        assertEquals(0, kits.activation().pageIndex());
    }

    @Test
    void switchPageIsNoOpWhenNotActive() {
        KitWorkflowDomainService kits = kits();
        assertFalse(kits.switchPage(1));
    }

    @Test
    void pageAsLoadoutUsesQuickAccessLaneId() {
        KitWorkflowDomainService kits = kits();
        ItemIdentity pickaxe = ItemIdentity.of("minecraft:iron_pickaxe");
        KitPage page = KitPage.empty().withSlot(0, pickaxe);
        KitDefinition kit = kits.create("Mining").withPages(List.of(page));
        kits.update(kit);

        QuickAccessLoadoutDefinition loadout = kits.pageAsLoadout(kits.kit(kit.id()), 0);

        assertNotNull(loadout);
        LoadoutTarget.QuickAccessLaneTarget target = (LoadoutTarget.QuickAccessLaneTarget)
                loadout.entries().iterator().next().target();
        assertEquals(BuiltinInventoryIds.QUICK_ACCESS_LANE_0, target.laneId());
    }

    @Test
    void pageAsLoadoutPreservesFilledSlotOrderAndIdentity() {
        KitWorkflowDomainService kits = kits();
        ItemIdentity pickaxe = ItemIdentity.of("minecraft:iron_pickaxe");
        ItemIdentity sword = ItemIdentity.of("minecraft:iron_sword");
        KitPage page = KitPage.empty().withSlot(0, pickaxe).withSlot(3, sword);
        KitDefinition kit = kits.create("Mining").withPages(List.of(page));
        kits.update(kit);

        QuickAccessLoadoutDefinition loadout = kits.pageAsLoadout(kits.kit(kit.id()), 0);

        assertNotNull(loadout);
        assertEquals(2, loadout.entries().size());
        boolean containsPickaxeAtSlot0 = loadout.entries().stream().anyMatch(entry ->
                entry.identity().equals(pickaxe)
                && entry.target() instanceof LoadoutTarget.QuickAccessLaneTarget laneTarget
                && laneTarget.slotIndex() == 0);
        boolean containsSwordAtSlot3 = loadout.entries().stream().anyMatch(entry ->
                entry.identity().equals(sword)
                && entry.target() instanceof LoadoutTarget.QuickAccessLaneTarget laneTarget
                && laneTarget.slotIndex() == 3);
        assertTrue(containsPickaxeAtSlot0);
        assertTrue(containsSwordAtSlot3);
    }

    @Test
    void planActivateReturnsEmptyPlanForUnknownKit() {
        KitWorkflowDomainService kits = kits();
        LoadoutApplyService.LoadoutApplyPlan plan = kits.planActivate(
                "missing",
                0,
                InventoryAuthoritySnapshot.empty(),
                ProtectionPolicy.allowAll(),
                null
        );
        assertTrue(plan.operations().isEmpty());
    }

    private static KitWorkflowDomainService kits() {
        return new KitWorkflowDomainService(new InMemoryWorkflowDomainStateRepository());
    }
}
