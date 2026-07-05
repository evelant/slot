package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.BuiltinInventoryDescriptors;
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.HostInstanceKey;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventoryStackSnapshot;
import dev.imagio.slot.inventory.core.InventoryTopologyDescriptor;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.PlayerRuntimeStateDescriptor;
import dev.imagio.slot.inventory.integration.InventoryHostObservationHints;
import dev.imagio.slot.inventory.integration.InventoryHostSession;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.testsupport.InventoryAuthorityFixtures;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkspaceAuthorityInvalidationsTest {
    @Test
    void carriedRevisionAcquireBecomesIdentityLocal() {
        WorkspaceInvalidation original = carriedRevisionFull();
        List<WorkspaceInvalidation> localized = WorkspaceAuthorityInvalidations.localizeCarriedRevisionInvalidations(
                authority(List.of()),
                authority(List.of(stack(0, "minecraft:stone", 16))),
                List.of(original));

        WorkspaceInvalidation invalidation = single(localized);
        assertFalse(invalidation.requiresFullProjection());
        assertEquals(WorkspaceInvalidation.Reason.CARRIED_REVISION_CHANGED, invalidation.reason());
        assertEquals(java.util.Set.of(ItemIdentity.of("minecraft:stone")), invalidation.identities());
        assertTrue(invalidation.slices().contains(WorkspaceProjectionSlice.CARD));
        assertTrue(invalidation.slices().contains(WorkspaceProjectionSlice.WAYFINDING));
        assertTrue(invalidation.slices().contains(WorkspaceProjectionSlice.DEPOSITABILITY));
        assertTrue(invalidation.slices().contains(WorkspaceProjectionSlice.CONTEXTUAL));
    }

    @Test
    void carriedRevisionRemovalKeepsRemovedIdentityLocal() {
        List<WorkspaceInvalidation> localized = WorkspaceAuthorityInvalidations.localizeCarriedRevisionInvalidations(
                authority(List.of(stack(0, "minecraft:stone", 16))),
                authority(List.of()),
                List.of(carriedRevisionFull()));

        WorkspaceInvalidation invalidation = single(localized);
        assertFalse(invalidation.requiresFullProjection());
        assertEquals(java.util.Set.of(ItemIdentity.of("minecraft:stone")), invalidation.identities());
    }

    @Test
    void carriedRevisionCountChangeMarksSameIdentityLocal() {
        List<WorkspaceInvalidation> localized = WorkspaceAuthorityInvalidations.localizeCarriedRevisionInvalidations(
                authority(List.of(stack(0, "minecraft:stone", 16))),
                authority(List.of(stack(0, "minecraft:stone", 20))),
                List.of(carriedRevisionFull()));

        WorkspaceInvalidation invalidation = single(localized);
        assertFalse(invalidation.requiresFullProjection());
        assertEquals(java.util.Set.of(ItemIdentity.of("minecraft:stone")), invalidation.identities());
    }

    @Test
    void carriedRevisionIdentitySwapMarksOldAndNewLocal() {
        List<WorkspaceInvalidation> localized = WorkspaceAuthorityInvalidations.localizeCarriedRevisionInvalidations(
                authority(List.of(stack(0, "minecraft:stone", 16))),
                authority(List.of(stack(0, "minecraft:dirt", 16))),
                List.of(carriedRevisionFull()));

        WorkspaceInvalidation invalidation = single(localized);
        assertFalse(invalidation.requiresFullProjection());
        assertEquals(
                java.util.Set.of(ItemIdentity.of("minecraft:stone"), ItemIdentity.of("minecraft:dirt")),
                invalidation.identities());
    }

    @Test
    void carriedRevisionWithNoAuthorityDeltaBecomesFrameOnly() {
        List<InventoryStackSnapshot> stacks = List.of(stack(0, "minecraft:stone", 16));
        List<WorkspaceInvalidation> localized = WorkspaceAuthorityInvalidations.localizeCarriedRevisionInvalidations(
                authority(stacks),
                authority(stacks),
                List.of(carriedRevisionFull()));

        WorkspaceInvalidation invalidation = single(localized);
        assertFalse(invalidation.requiresFullProjection());
        assertTrue(invalidation.identities().isEmpty());
        assertEquals(java.util.EnumSet.of(WorkspaceProjectionSlice.FRAME), invalidation.slices());
        assertEquals("carried_revision_no_identity_delta", invalidation.diagnostics());
    }

    @Test
    void carriedRevisionWithoutPreviousAuthorityStaysFull() {
        WorkspaceInvalidation original = carriedRevisionFull();
        List<WorkspaceInvalidation> localized = WorkspaceAuthorityInvalidations.localizeCarriedRevisionInvalidations(
                null,
                authority(List.of(stack(0, "minecraft:stone", 16))),
                List.of(original));

        assertSame(original, single(localized));
    }

    @Test
    void carriedRevisionWithSourceShapeChangeStaysFull() {
        WorkspaceInvalidation original = carriedRevisionFull();
        List<WorkspaceInvalidation> localized = WorkspaceAuthorityInvalidations.localizeCarriedRevisionInvalidations(
                authority(List.of(stack(0, "minecraft:stone", 16))),
                authority(List.of(stack(0, "minecraft:stone", 20)),
                        Map.of(BuiltinInventoryIds.PLAYER_MAIN, 40)),
                List.of(original));

        assertSame(original, single(localized));
    }

    @Test
    void carriedRevisionWithLocalizedMenuHintCoalescesWithoutFullProjection() {
        WorkspaceInvalidation menuHint = WorkspaceInvalidation.localizedIdentity(
                WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED,
                ItemIdentity.of("minecraft:dirt"),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.HOTBAR,
                        WorkspaceProjectionSlice.WORKFLOW,
                        WorkspaceProjectionSlice.FRAME),
                "menu_slot_changed");
        List<WorkspaceInvalidation> localized = WorkspaceAuthorityInvalidations.localizeCarriedRevisionInvalidations(
                authority(List.of(stack(0, "minecraft:stone", 16))),
                authority(List.of(stack(0, "minecraft:dirt", 16))),
                List.of(menuHint, carriedRevisionFull()));

        WorkspaceInvalidationSummary summary = WorkspaceInvalidationSummary.coalesce(localized);
        assertFalse(summary.requiresFullProjection());
        assertEquals(
                java.util.Set.of(ItemIdentity.of("minecraft:stone"), ItemIdentity.of("minecraft:dirt")),
                summary.identities());
    }

    private static WorkspaceInvalidation single(List<WorkspaceInvalidation> invalidations) {
        assertEquals(1, invalidations.size());
        return invalidations.get(0);
    }

    private static WorkspaceInvalidation carriedRevisionFull() {
        return WorkspaceInvalidation.full(
                WorkspaceInvalidation.Reason.CARRIED_REVISION_CHANGED,
                "carried_revision_changed_not_localized");
    }

    private static InventoryAuthoritySnapshot authority(List<InventoryStackSnapshot> stacks) {
        return authority(stacks, Map.of());
    }

    private static InventoryAuthoritySnapshot authority(
            List<InventoryStackSnapshot> stacks,
            Map<String, Integer> capacities
    ) {
        return InventoryAuthorityFixtures.authority(
                host(),
                Map.of(BuiltinInventoryIds.PLAYER_MAIN, stacks),
                capacities);
    }

    private static InventoryStackSnapshot stack(int slot, String itemId, int count) {
        return new InventoryStackSnapshot(slot, new ItemStack(itemId, count, 64), count);
    }

    private static InventoryHostDescriptor host() {
        TestMenu menu = new TestMenu();
        InventoryTopologyDescriptor topology = InventoryTopologyDescriptor.empty();
        return new InventoryHostDescriptor(
                new HostInstanceKey(TestMenu.class.getName(), 0, "slot.workspace.authority-invalidations.test", ""),
                InventoryHostDescriptor.serverMenuRef(menu),
                "slot.workspace.authority-invalidations.test",
                Component.literal("Workspace Authority Invalidations Test"),
                menu,
                topology,
                InventoryHostSession.empty(),
                List.of(),
                PlayerRuntimeStateDescriptor.vanilla(0),
                BuiltinInventoryDescriptors.builtInPlayerSources(topology),
                BuiltinInventoryDescriptors.builtInQuickAccessLanes(),
                BuiltinInventoryDescriptors.builtInEquipmentGroups(),
                List.of(),
                InventoryHostObservationHints.defaults(),
                "");
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
