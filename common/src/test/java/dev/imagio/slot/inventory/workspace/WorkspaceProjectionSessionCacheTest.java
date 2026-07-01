package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.BuiltinInventoryDescriptors;
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.HostInstanceKey;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventoryStackSnapshot;
import dev.imagio.slot.inventory.core.InventoryTopologyDescriptor;
import dev.imagio.slot.inventory.core.PlayerRuntimeStateDescriptor;
import dev.imagio.slot.inventory.integration.InventoryHostObservationHints;
import dev.imagio.slot.inventory.integration.InventoryHostSession;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.testsupport.InventoryAuthorityFixtures;
import dev.imagio.slot.workflow.domain.WorkflowDomainSnapshot;
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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkspaceProjectionSessionCacheTest {
    @Test
    void unchangedStructuralInputReusesProjection() {
        WorkspaceProjectionSessionCache cache = new WorkspaceProjectionSessionCache();
        WorkspaceProjectionRequest request = request(authority("minecraft:stone", 16), "ready", "", 0, 0, "");

        WorkspaceProjectionResult first = cache.project(request);
        WorkspaceProjectionResult second = cache.project(request);

        assertFalse(first.structuralCacheHit());
        assertTrue(second.structuralCacheHit());
        assertEquals(first.contentFingerprint(), second.contentFingerprint());
        assertEquals(1, second.diagnostics().structuralHits());
        assertEquals(1, second.diagnostics().structuralMisses());
    }

    @Test
    void frameOnlyChangeReusesStructuralProjectionButChangesContentFingerprint() {
        WorkspaceProjectionSessionCache cache = new WorkspaceProjectionSessionCache();
        WorkspaceProjectionResult first = cache.project(
                request(authority("minecraft:stone", 16), "ready", "", 0, 0, ""));
        WorkspaceProjectionResult second = cache.project(
                request(authority("minecraft:stone", 16), "busy", "transfer_pending", 1, 1, ""));

        assertFalse(first.structuralCacheHit());
        assertTrue(second.structuralCacheHit());
        assertNotEquals(first.contentFingerprint(), second.contentFingerprint());
        assertEquals("busy", second.viewModel().status());
        assertEquals("transfer_pending", second.viewModel().diagnostics());
        assertEquals(1, second.viewModel().pendingCount());
        assertEquals(1, second.viewModel().selectedQuickAccessSlot());
    }

    @Test
    void carriedCountChangeInvalidatesStructuralProjection() {
        WorkspaceProjectionSessionCache cache = new WorkspaceProjectionSessionCache();
        WorkspaceProjectionResult first = cache.project(
                request(authority("minecraft:stone", 16), "ready", "", 0, 0, ""));
        WorkspaceProjectionResult second = cache.project(
                request(authority("minecraft:stone", 17), "ready", "", 0, 0, ""));

        assertFalse(first.structuralCacheHit());
        assertFalse(second.structuralCacheHit());
        assertNotEquals(first.contentFingerprint(), second.contentFingerprint());
        assertEquals(2, second.diagnostics().structuralMisses());
    }

    @Test
    void identityMemoSurvivesAcrossStructuralMisses() {
        WorkspaceProjectionSessionCache cache = new WorkspaceProjectionSessionCache();
        String patchouliFingerprint = "{Patchouli:Book=>slot:test_book,Damage=12}";

        WorkspaceProjectionResult first = cache.project(
                request(authority("patchouli:guide_book", patchouliFingerprint, 1), "ready", "", 0, 0, "a"));
        WorkspaceProjectionResult second = cache.project(
                request(authority("patchouli:guide_book", patchouliFingerprint, 1), "ready", "", 0, 0, "b"));

        assertFalse(first.structuralCacheHit());
        assertFalse(second.structuralCacheHit());
        assertTrue(
                second.identityMemoStats().createHits() > first.identityMemoStats().createHits()
                        || second.identityMemoStats().normalizeHits() > first.identityMemoStats().normalizeHits(),
                "expected identity memo hits after a second projection over the same component fingerprint");
    }

    private static WorkspaceProjectionRequest request(
            InventoryAuthoritySnapshot authority,
            String status,
            String diagnostics,
            int pendingCount,
            int selectedSlot,
            String searchQuery
    ) {
        return new WorkspaceProjectionRequest(
                authority,
                WorkflowDomainSnapshot.empty(),
                status,
                diagnostics,
                pendingCount,
                selectedSlot,
                0,
                null,
                null,
                null,
                Set.of(),
                null,
                null,
                searchQuery,
                null,
                0L,
                SlotWorkspaceViewModel.ActiveChestPanel.empty(),
                List.of(),
                Set.of(),
                List.of(),
                List.of(),
                Set.of(),
                WorkspaceStorageIndex.empty(),
                null,
                null);
    }

    private static InventoryAuthoritySnapshot authority(String itemId, int count) {
        return authority(itemId, "", count);
    }

    private static InventoryAuthoritySnapshot authority(String itemId, String componentFingerprint, int count) {
        return InventoryAuthorityFixtures.authority(
                host(),
                Map.of(BuiltinInventoryIds.PLAYER_MAIN, List.of(new InventoryStackSnapshot(
                        0,
                        new ItemStack(itemId, componentFingerprint, count, 64),
                        count))),
                Map.of(BuiltinInventoryIds.PLAYER_MAIN, 36));
    }

    private static InventoryHostDescriptor host() {
        TestMenu menu = new TestMenu();
        InventoryTopologyDescriptor topology = InventoryTopologyDescriptor.empty();
        return new InventoryHostDescriptor(
                new HostInstanceKey(TestMenu.class.getName(), 0, "slot.workspace.projection-cache.test", ""),
                InventoryHostDescriptor.serverMenuRef(menu),
                "slot.workspace.projection-cache.test",
                Component.literal("Workspace Projection Cache Test"),
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
