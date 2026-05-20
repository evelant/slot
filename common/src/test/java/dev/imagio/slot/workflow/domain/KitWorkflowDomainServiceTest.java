package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

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
    void renameRejectsDuplicateSiblingName() {
        KitWorkflowDomainService kits = kits();
        kits.create("Mining");
        KitDefinition combat = kits.create("Combat");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> kits.rename(combat.id(), "Mining")
        );

        assertEquals("Workflow name already exists: Mining", exception.getMessage());
        assertEquals("Combat", kits.kit(combat.id()).name());
    }

    @Test
    void updateReplacesPages() {
        KitWorkflowDomainService kits = kits();
        KitDefinition kit = kits.create("Combat");
        ItemIdentity sword = ItemIdentity.of("minecraft:iron_sword");

        KitPage page = KitPage.empty().withSlot(0, sword);
        KitDefinition next = kit.withPages(List.of(page));

        assertTrue(kits.update(next));
        KitDefinition stored = kits.kit(kit.id());
        assertEquals(sword, stored.page(0).slot(0));
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
    void activateStoresPutAwaySnapshot() {
        KitWorkflowDomainService kits = kits();
        KitDefinition kit = kits.create("Mining");
        ItemIdentity dirt = ItemIdentity.of("minecraft:dirt");

        assertTrue(kits.activate(kit.id(), 0, Set.of(dirt)));

        assertEquals(Set.of(dirt), kits.activation().putAwayIdentities());
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
        kits.activate(kit.id(), 0, Set.of(ItemIdentity.of("minecraft:dirt")));

        assertTrue(kits.switchPage(1));
        assertEquals(1, kits.activation().pageIndex());
        assertEquals(Set.of(ItemIdentity.of("minecraft:dirt")), kits.activation().putAwayIdentities());
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
    void duplicateClonesPagesAndOffhandWithUniqueId() {
        KitWorkflowDomainService kits = kits();
        ItemIdentity pick = ItemIdentity.of("minecraft:iron_pickaxe");
        KitPage page = KitPage.empty().withSlot(0, pick);
        KitDefinition original = kits.create("Mining")
                .withPages(List.of(page))
                .withOffhand(ItemIdentity.of("minecraft:shield"));
        kits.update(original);

        KitDefinition copy = kits.duplicate(original.id());

        assertNotNull(copy);
        assertEquals("mining-copy", copy.id());
        assertEquals("Mining (copy)", copy.name());
        assertEquals(pick, copy.page(0).slot(0));
        assertEquals(ItemIdentity.of("minecraft:shield"), copy.offhand());
        assertEquals(2, kits.kits().size());
    }

    @Test
    void duplicateUsesReadableNamesAndStaysBesideSourceFamily() {
        KitWorkflowDomainService kits = kits();
        KitDefinition mining = kits.create("Mining");
        KitDefinition building = kits.create("Building");

        KitDefinition firstCopy = kits.duplicate(mining.id());
        KitDefinition secondCopy = kits.duplicate(mining.id());
        KitDefinition copyOfCopy = kits.duplicate(firstCopy.id());

        assertEquals("Mining (copy)", firstCopy.name());
        assertEquals("Mining (copy 2)", secondCopy.name());
        assertEquals("Mining (copy 3)", copyOfCopy.name());
        assertEquals(
                List.of(mining.id(), firstCopy.id(), secondCopy.id(), copyOfCopy.id(), building.id()),
                topLevelIds(kits.kits())
        );
    }

    @Test
    void reorderMovesTopLevelWorkflowsWithoutChangingVariantParentage() {
        KitWorkflowDomainService kits = kits();
        KitDefinition mining = kits.create("Mining");
        KitDefinition combat = kits.create("Combat");
        KitDefinition farming = kits.create("Farming");
        KitDefinition deepMining = kits.createVariant(mining.id(), "Deep Mining");

        assertTrue(kits.reorder(farming.id(), 0));

        assertEquals(
                List.of(farming.id(), mining.id(), combat.id()),
                topLevelIds(kits.kits())
        );
        assertEquals(List.of(deepMining.id()), kitIds(kits.kitMap().variantsOf(mining.id())));
    }

    @Test
    void reorderMovesVariantsOnlyWithinTheirParent() {
        KitWorkflowDomainService kits = kits();
        KitDefinition mining = kits.create("Mining");
        KitDefinition combat = kits.create("Combat");
        KitDefinition deepMining = kits.createVariant(mining.id(), "Deep Mining");
        KitDefinition netherMining = kits.createVariant(mining.id(), "Nether Mining");
        KitDefinition bossPrep = kits.createVariant(combat.id(), "Boss Prep");

        assertTrue(kits.reorder(netherMining.id(), 0));

        assertEquals(
                List.of(netherMining.id(), deepMining.id()),
                kitIds(kits.kitMap().variantsOf(mining.id()))
        );
        assertEquals(List.of(bossPrep.id()), kitIds(kits.kitMap().variantsOf(combat.id())));
    }

    @Test
    void reorderReturnsFalseWhenWorkflowAlreadyAtTarget() {
        KitWorkflowDomainService kits = kits();
        KitDefinition mining = kits.create("Mining");
        kits.create("Combat");

        assertFalse(kits.reorder(mining.id(), 0));
    }

    @Test
    void addPageAppendsEmptyPageAndPreservesExisting() {
        KitWorkflowDomainService kits = kits();
        KitPage first = KitPage.empty().withSlot(0, ItemIdentity.of("minecraft:pickaxe"));
        KitDefinition kit = kits.create("Multi").withPages(List.of(first));
        kits.update(kit);

        assertTrue(kits.addPage(kit.id()));

        KitDefinition stored = kits.kit(kit.id());
        assertEquals(2, stored.pageCount());
        assertEquals(first, stored.page(0));
        assertEquals(0, stored.page(1).filledSlotCount());
    }

    @Test
    void addPageRejectsWhenCapacityExceeded() {
        KitWorkflowDomainService kits = kits();
        KitDefinition kit = kits.create("Big");
        // 4 pages * 9 = 36 is the cap, so a 5th page would bust it
        kits.update(kit.withPages(List.of(KitPage.empty(), KitPage.empty(), KitPage.empty(), KitPage.empty())));
        assertThrows(IllegalArgumentException.class, () -> kits.addPage(kit.id()));
    }

    @Test
    void removePageSlidesActivationBackWhenRemovingActivePage() {
        KitWorkflowDomainService kits = kits();
        KitPage p0 = KitPage.empty().withSlot(0, ItemIdentity.of("minecraft:pickaxe"));
        KitPage p1 = KitPage.empty().withSlot(0, ItemIdentity.of("minecraft:sword"));
        KitPage p2 = KitPage.empty().withSlot(0, ItemIdentity.of("minecraft:bow"));
        KitDefinition kit = kits.create("Multi").withPages(List.of(p0, p1, p2));
        kits.update(kit);
        kits.activate(kit.id());
        kits.switchPage(2);
        assertEquals(2, kits.activation().pageIndex());

        assertTrue(kits.removePage(kit.id(), 2));

        KitDefinition after = kits.kit(kit.id());
        assertEquals(2, after.pageCount());
        assertEquals(1, kits.activation().pageIndex());
    }

    @Test
    void removePageIsNoOpOnLastPage() {
        KitWorkflowDomainService kits = kits();
        KitDefinition kit = kits.create("Single");
        assertFalse(kits.removePage(kit.id(), 0));
        assertEquals(1, kits.kit(kit.id()).pageCount());
    }

    @Test
    void swapSlotsExchangesTwoIdentitiesOnAPage() {
        KitWorkflowDomainService kits = kits();
        ItemIdentity pick = ItemIdentity.of("minecraft:iron_pickaxe");
        ItemIdentity sword = ItemIdentity.of("minecraft:iron_sword");
        KitPage page = KitPage.empty().withSlot(0, pick).withSlot(3, sword);
        KitDefinition kit = kits.create("Mix").withPages(List.of(page));
        kits.update(kit);

        assertTrue(kits.swapSlots(kit.id(), 0, 0, 3));

        KitDefinition stored = kits.kit(kit.id());
        assertEquals(sword, stored.page(0).slot(0));
        assertEquals(pick, stored.page(0).slot(3));
    }

    @Test
    void swapSlotsIsNoOpWhenIndicesMatch() {
        KitWorkflowDomainService kits = kits();
        KitDefinition kit = kits.create("Mix");
        assertFalse(kits.swapSlots(kit.id(), 0, 1, 1));
    }

    @Test
    void setSlotIdentityUpdatesPageSlot() {
        KitWorkflowDomainService kits = kits();
        KitDefinition kit = kits.create("Mining");
        ItemIdentity pick = ItemIdentity.of("minecraft:iron_pickaxe");

        assertTrue(kits.setSlotIdentity(kit.id(), 0, 3, pick));
        assertEquals(pick, kits.kit(kit.id()).page(0).slot(3));
    }

    @Test
    void kitActiveProtectionProtectsBeltAndKitDesiredCountIdentitiesFromTrash() {
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        KitWorkflowDomainService kits = new KitWorkflowDomainService(repository);
        DesiredCountWorkflowDomainService desired = new DesiredCountWorkflowDomainService(repository, () -> {});
        ItemIdentity pick = ItemIdentity.of("minecraft:iron_pickaxe");
        ItemIdentity torch = ItemIdentity.of("minecraft:torch");
        KitPage page = KitPage.empty().withSlot(0, pick);
        KitDefinition kit = kits.create("Mining").withPages(List.of(page));
        kits.update(kit);
        kits.activate(kit.id());
        desired.setForKit(kit.id(), torch, 4);

        dev.imagio.slot.workflow.domain.KitActiveProtection protection =
                new dev.imagio.slot.workflow.domain.KitActiveProtection(
                        ProtectionPolicy.allowAll(),
                        dev.imagio.slot.workflow.domain.KitActiveProtection.identitiesFor(
                                kits.kitMap(), repository.workflowProjection().kitDesiredCounts())
                );

        assertTrue(protection.protects(pick, dev.imagio.slot.inventory.action.InventoryActionKind.TRASH));
        assertTrue(protection.protects(torch, dev.imagio.slot.inventory.action.InventoryActionKind.VOID));
        assertFalse(protection.protects(pick, dev.imagio.slot.inventory.action.InventoryActionKind.TRANSFER));
    }

    @Test
    void kitActiveProtectionIsEmptyWhenNoneActive() {
        KitWorkflowDomainService kits = kits();
        kits.create("Mining");
        assertTrue(dev.imagio.slot.workflow.domain.KitActiveProtection.identitiesFor(kits.kitMap()).isEmpty());
    }

    @Test
    void updateRejectsCapacityOverflow() {
        KitWorkflowDomainService kits = kits();
        KitDefinition kit = kits.create("Big");
        List<KitPage> fivePages = List.of(
                KitPage.empty(), KitPage.empty(), KitPage.empty(), KitPage.empty(), KitPage.empty()
        );
        assertThrows(IllegalArgumentException.class, () -> kits.update(kit.withPages(fivePages)));
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
    void pageAsLoadoutIncludesOffhandPin() {
        KitWorkflowDomainService kits = kits();
        ItemIdentity pickaxe = ItemIdentity.of("minecraft:iron_pickaxe");
        ItemIdentity shield = ItemIdentity.of("minecraft:shield");
        KitPage page = KitPage.empty().withSlot(0, pickaxe);
        KitDefinition kit = kits.create("Mining")
                .withPages(List.of(page))
                .withOffhand(shield);
        kits.update(kit);

        QuickAccessLoadoutDefinition loadout = kits.pageAsLoadout(kits.kit(kit.id()), 0);

        assertNotNull(loadout);
        assertEquals(2, loadout.entries().size());
        assertTrue(loadout.entries().contains(new QuickAccessLoadoutEntry(
                new LoadoutTarget.QuickAccessLaneTarget(BuiltinInventoryIds.QUICK_ACCESS_LANE_0, 0),
                pickaxe
        )));
        assertTrue(loadout.entries().contains(new QuickAccessLoadoutEntry(
                new LoadoutTarget.EquipmentSlotTarget(BuiltinInventoryIds.EQUIPMENT_GROUP_OFFHAND, 0),
                shield
        )));
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

    private static List<String> topLevelIds(List<KitDefinition> kits) {
        return kits.stream()
                .filter(kit -> !kit.variant())
                .map(KitDefinition::id)
                .toList();
    }

    private static List<String> kitIds(List<KitDefinition> kits) {
        return kits.stream()
                .map(KitDefinition::id)
                .toList();
    }
}
