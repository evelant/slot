package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.BuiltinInventoryDescriptors;
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.HostInstanceKey;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventoryStackSnapshot;
import dev.imagio.slot.inventory.core.InventoryTopologyDescriptor;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemComparisonMode;
import dev.imagio.slot.inventory.core.PlayerRuntimeStateDescriptor;
import dev.imagio.slot.inventory.integration.InventoryHostObservationHints;
import dev.imagio.slot.inventory.integration.InventoryHostSession;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.CursorStateSnapshot;
import dev.imagio.slot.inventory.storage.WorldDisplayStorageKind;
import dev.imagio.slot.inventory.storage.WorldDisplayStorageSource;
import dev.imagio.slot.inventory.storage.WorldStorageAccess;
import dev.imagio.slot.ui.workspace.RecipeIngredientSidebarSpec;
import dev.imagio.slot.workflow.domain.ChestAffinity;
import dev.imagio.slot.workflow.domain.ChestAffinityMap;
import dev.imagio.slot.testsupport.InventoryAuthorityFixtures;
import dev.imagio.slot.workflow.domain.ChestAnchor;
import dev.imagio.slot.workflow.domain.ChestRole;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import dev.imagio.slot.workflow.domain.ClaimedChestMap;
import dev.imagio.slot.workflow.domain.CraftRunAlternative;
import dev.imagio.slot.workflow.domain.CraftRunIngredientGroup;
import dev.imagio.slot.workflow.domain.CraftRunRecipeEntry;
import dev.imagio.slot.workflow.domain.CraftRunState;
import dev.imagio.slot.workflow.domain.KitActivation;
import dev.imagio.slot.workflow.domain.KitDefinition;
import dev.imagio.slot.workflow.domain.KitMap;
import dev.imagio.slot.workflow.domain.KitPage;
import dev.imagio.slot.workflow.domain.VisualAtlasIsland;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import dev.imagio.slot.workflow.domain.VisualHomeAssignment;
import dev.imagio.slot.workflow.domain.VisualHomeMap;
import dev.imagio.slot.workflow.domain.VisualHomeOrigin;
import dev.imagio.slot.workflow.domain.WorkflowAcceptedInputRule;
import dev.imagio.slot.workflow.domain.WorkflowDomainSnapshot;
import dev.imagio.slot.workflow.domain.WorkflowProjection;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkspaceProjectionSessionCacheTest {
    @AfterEach
    void resetGhostStackResolver() {
        SlotWorkspaceViewModel.setGhostStackResolver(null);
    }

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
        assertEquals(7, first.diagnostics().projectionSliceStats().rebuiltSlices());
        assertEquals(7, second.diagnostics().projectionSliceStats().reusedSlices());
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
        assertTrue(second.diagnostics().projectionSliceStats().reusedSlices() >= 5);
        assertEquals(7,
                second.diagnostics().projectionSliceStats().rebuiltSlices()
                        + second.diagnostics().projectionSliceStats().reusedSlices());
    }

    @Test
    void statusOnlyChangeRebuildsFrameSliceOnly() {
        WorkspaceProjectionSessionCache cache = new WorkspaceProjectionSessionCache();
        cache.project(request(authority("minecraft:stone", 16), "ready", "", 0, 0, ""));

        WorkspaceProjectionResult second = cache.project(
                request(authority("minecraft:stone", 16), "busy", "transfer_pending", 0, 0, ""));

        assertTrue(second.structuralCacheHit());
        assertEquals(1, second.diagnostics().projectionSliceStats().rebuiltSlices());
        assertEquals(6, second.diagnostics().projectionSliceStats().reusedSlices());
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
    void changedIdentityRebuildsOnlyThatCard() {
        WorkspaceProjectionSessionCache cache = new WorkspaceProjectionSessionCache();
        WorkspaceProjectionResult first = cache.project(request(authority(List.of(
                stack(0, "minecraft:stone", 16),
                stack(1, "minecraft:dirt", 4))), "ready", "", 0, 0, ""));
        WorkspaceProjectionResult second = cache.project(
                request(authority(List.of(
                        stack(0, "minecraft:stone", 17),
                        stack(1, "minecraft:dirt", 4))), "ready", "", 0, 0, ""),
                WorkspaceInvalidation.identityLocal(
                        WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED,
                        new ItemIdentity("minecraft:stone", ItemComparisonMode.ITEM_ID, ""),
                        java.util.EnumSet.of(WorkspaceProjectionSlice.CARD, WorkspaceProjectionSlice.SECTION),
                        "test_identity_card_change"));

        assertFalse(second.structuralCacheHit());
        assertEquals(1, second.diagnostics().cardProjectionStats().reusedCards());
        assertEquals(1, second.diagnostics().cardProjectionStats().rebuiltCards());
        assertEquals(0, second.diagnostics().cardProjectionStats().removedCards());
        assertSame(card(first.viewModel(), "minecraft:dirt"), card(second.viewModel(), "minecraft:dirt"));
        assertNotSame(card(first.viewModel(), "minecraft:stone"), card(second.viewModel(), "minecraft:stone"));
    }

    @Test
    void acquiringIdentityReusesExistingCardsAndBuildsNewCard() {
        WorkspaceProjectionSessionCache cache = new WorkspaceProjectionSessionCache();
        WorkspaceProjectionResult first = cache.project(request(authority(List.of(
                stack(0, "minecraft:stone", 16))), "ready", "", 0, 0, ""));
        WorkspaceProjectionResult second = cache.project(
                request(authority(List.of(
                        stack(0, "minecraft:stone", 16),
                        stack(1, "minecraft:dirt", 4))), "ready", "", 0, 0, ""),
                WorkspaceInvalidation.identityLocal(
                        WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED,
                        new ItemIdentity("minecraft:dirt", ItemComparisonMode.ITEM_ID, ""),
                        java.util.EnumSet.of(WorkspaceProjectionSlice.CARD, WorkspaceProjectionSlice.SECTION),
                        "test_identity_card_add"));

        assertEquals(1, second.diagnostics().cardProjectionStats().reusedCards());
        assertEquals(1, second.diagnostics().cardProjectionStats().rebuiltCards());
        assertEquals(0, second.diagnostics().cardProjectionStats().removedCards());
        assertSame(card(first.viewModel(), "minecraft:stone"), card(second.viewModel(), "minecraft:stone"));
    }

    @Test
    void losingIdentityReportsRemovedCardWithoutRebuildingSiblings() {
        WorkspaceProjectionSessionCache cache = new WorkspaceProjectionSessionCache();
        WorkspaceProjectionResult first = cache.project(request(authority(List.of(
                stack(0, "minecraft:stone", 16),
                stack(1, "minecraft:dirt", 4))), "ready", "", 0, 0, ""));
        WorkspaceProjectionResult second = cache.project(
                request(authority(List.of(
                        stack(0, "minecraft:stone", 16))), "ready", "", 0, 0, ""),
                WorkspaceInvalidation.identityLocal(
                        WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED,
                        new ItemIdentity("minecraft:dirt", ItemComparisonMode.ITEM_ID, ""),
                        java.util.EnumSet.of(WorkspaceProjectionSlice.CARD, WorkspaceProjectionSlice.SECTION),
                        "test_identity_card_remove"));

        assertEquals(1, second.diagnostics().cardProjectionStats().reusedCards());
        assertEquals(0, second.diagnostics().cardProjectionStats().rebuiltCards());
        assertEquals(1, second.diagnostics().cardProjectionStats().removedCards());
        assertSame(card(first.viewModel(), "minecraft:stone"), card(second.viewModel(), "minecraft:stone"));
    }

    @Test
    void changedStorageRebuildsOnlyThatStorageChip() {
        WorkspaceProjectionSessionCache cache = new WorkspaceProjectionSessionCache();
        WorkspaceProjectionResult first = cache.project(storageRequest(storageIndex(List.of(
                displayStorageEntry("storage-a", 1, "minecraft:stone", 4),
                displayStorageEntry("storage-b", 2, "minecraft:dirt", 9)), 1L)));
        WorkspaceProjectionResult second = cache.project(
                storageRequest(storageIndex(List.of(
                        displayStorageEntry("storage-a", 1, "minecraft:stone", 5),
                        displayStorageEntry("storage-b", 2, "minecraft:dirt", 9)), 2L)),
                WorkspaceInvalidation.storageLocal(
                        WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED,
                        "storage-a",
                        java.util.EnumSet.of(WorkspaceProjectionSlice.STORAGE, WorkspaceProjectionSlice.CARD),
                        "test_storage_chip_change"));

        assertFalse(second.structuralCacheHit());
        assertEquals(1, second.diagnostics().storageProjectionStats().reusedStorageChips());
        assertEquals(1, second.diagnostics().storageProjectionStats().rebuiltStorageChips());
        assertEquals(0, second.diagnostics().storageProjectionStats().removedStorageChips());
        assertSame(chip(first.viewModel(), "storage-b"), chip(second.viewModel(), "storage-b"));
        assertNotSame(chip(first.viewModel(), "storage-a"), chip(second.viewModel(), "storage-a"));
    }

    @Test
    void losingStorageReportsRemovedChipWithoutRebuildingSiblings() {
        WorkspaceProjectionSessionCache cache = new WorkspaceProjectionSessionCache();
        WorkspaceProjectionResult first = cache.project(storageRequest(storageIndex(List.of(
                displayStorageEntry("storage-a", 1, "minecraft:stone", 4),
                displayStorageEntry("storage-b", 2, "minecraft:dirt", 9)), 1L)));
        WorkspaceProjectionResult second = cache.project(
                storageRequest(storageIndex(List.of(
                        displayStorageEntry("storage-b", 2, "minecraft:dirt", 9)), 2L)),
                WorkspaceInvalidation.storageLocal(
                        WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED,
                        "storage-a",
                        java.util.EnumSet.of(WorkspaceProjectionSlice.STORAGE),
                        "test_storage_chip_remove"));

        assertEquals(1, second.diagnostics().storageProjectionStats().reusedStorageChips());
        assertEquals(0, second.diagnostics().storageProjectionStats().rebuiltStorageChips());
        assertEquals(1, second.diagnostics().storageProjectionStats().removedStorageChips());
        assertSame(chip(first.viewModel(), "storage-b"), chip(second.viewModel(), "storage-b"));
    }

    @Test
    void simpleStorageInvalidationSkipsFullProjectionAndMatchesOracle() {
        WorkspaceProjectionRequest seed = storageRequest(storageIndex(List.of(
                displayStorageEntry("storage-a", 1, "minecraft:stone", 4),
                displayStorageEntry("storage-b", 2, "minecraft:dirt", 9)), 1L));
        WorkspaceProjectionRequest changed = storageRequest(storageIndex(List.of(
                displayStorageEntry("storage-a", 1, "minecraft:stone", 5),
                displayStorageEntry("storage-b", 2, "minecraft:dirt", 9)), 2L));

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(WorkspaceInvalidation.localizedStorage(
                        WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED,
                        "storage-a",
                        java.util.EnumSet.of(WorkspaceProjectionSlice.STORAGE),
                        "simple_storage_chip_change")));
        WorkspaceProjectionResult result = parity.invalidated();

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertEquals(5, chip(result.viewModel(), "storage-a").contents().get(0).count());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleStorageRemovalSkipsFullProjectionAndMatchesOracle() {
        WorkspaceProjectionRequest seed = storageRequest(storageIndex(List.of(
                displayStorageEntry("storage-a", 1, "minecraft:stone", 4),
                displayStorageEntry("storage-b", 2, "minecraft:dirt", 9)), 1L));
        WorkspaceProjectionRequest changed = storageRequest(storageIndex(List.of(
                displayStorageEntry("storage-b", 2, "minecraft:dirt", 9)), 2L));

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(WorkspaceInvalidation.localizedStorage(
                        WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED,
                        "storage-a",
                        java.util.EnumSet.of(WorkspaceProjectionSlice.STORAGE),
                        "simple_storage_chip_remove")));
        WorkspaceProjectionResult result = parity.invalidated();

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertFalse(hasChip(result.viewModel(), "storage-a"));
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleStorageInvalidationWithCarriedCardsSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity planks = new ItemIdentity("minecraft:oak_planks", ItemComparisonMode.ITEM_ID, "");
        WorkflowDomainSnapshot workflow = workflowWithHomes(homeMap(planks));
        InventoryAuthoritySnapshot authority = authority(List.of(
                stack(0, "minecraft:oak_planks", 16),
                stack(1, "minecraft:stick", 8)));
        WorkspaceProjectionRequest seed = storageRequest(
                authority,
                workflow,
                storageIndex(List.of(
                        displayStorageEntry("storage-a", 1, "minecraft:stone", 4),
                        displayStorageEntry("storage-b", 2, "minecraft:dirt", 9)), 1L));
        WorkspaceProjectionRequest changed = storageRequest(
                authority,
                workflow,
                storageIndex(List.of(
                        displayStorageEntry("storage-a", 1, "minecraft:stone", 5),
                        displayStorageEntry("storage-b", 2, "minecraft:dirt", 9)), 2L));

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(WorkspaceInvalidation.localizedStorage(
                        WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED,
                        "storage-a",
                        java.util.EnumSet.of(WorkspaceProjectionSlice.STORAGE),
                        "simple_storage_chip_change_with_cards")));
        WorkspaceProjectionResult result = parity.invalidated();

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos());
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertEquals("materials", card(result.viewModel(), "minecraft:oak_planks").islandId());
        assertEquals(SlotWorkspaceAtlasLayout.ISLAND_TRIAGE, card(result.viewModel(), "minecraft:stick").islandId());
        assertEquals(5, chip(result.viewModel(), "storage-a").contents().get(0).count());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleStorageRemovalWithCarriedCardsSkipsFullProjectionAndMatchesOracle() {
        InventoryAuthoritySnapshot authority = authority(List.of(
                stack(0, "minecraft:oak_planks", 16),
                stack(1, "minecraft:stick", 8)));
        WorkspaceProjectionRequest seed = storageRequest(
                authority,
                WorkflowDomainSnapshot.empty(),
                storageIndex(List.of(
                        displayStorageEntry("storage-a", 1, "minecraft:stone", 4),
                        displayStorageEntry("storage-b", 2, "minecraft:dirt", 9)), 1L));
        WorkspaceProjectionRequest changed = storageRequest(
                authority,
                WorkflowDomainSnapshot.empty(),
                storageIndex(List.of(
                        displayStorageEntry("storage-b", 2, "minecraft:dirt", 9)), 2L));

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(WorkspaceInvalidation.localizedStorage(
                        WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED,
                        "storage-a",
                        java.util.EnumSet.of(WorkspaceProjectionSlice.STORAGE),
                        "simple_storage_chip_remove_with_cards")));
        WorkspaceProjectionResult result = parity.invalidated();

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos());
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertFalse(hasChip(result.viewModel(), "storage-a"));
        assertEquals(16, card(result.viewModel(), "minecraft:oak_planks").totalCount());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void storageOnlyInvalidationFallsBackWhenAuthorityAlsoChanged() {
        WorkspaceProjectionSessionCache cache = new WorkspaceProjectionSessionCache();
        WorkspaceProjectionRequest seed = storageRequest(
                authority("minecraft:oak_planks", 16),
                WorkflowDomainSnapshot.empty(),
                storageIndex(List.of(
                        displayStorageEntry("storage-a", 1, "minecraft:stone", 4)), 1L));
        WorkspaceProjectionRequest changed = storageRequest(
                authority("minecraft:oak_planks", 17),
                WorkflowDomainSnapshot.empty(),
                storageIndex(List.of(
                        displayStorageEntry("storage-a", 1, "minecraft:stone", 5)), 2L));

        cache.project(seed, WorkspaceInvalidation.full(
                WorkspaceInvalidation.Reason.SESSION_OPEN,
                "authority_guard_seed"));
        WorkspaceProjectionResult result = cache.project(changed, WorkspaceInvalidation.localizedStorage(
                WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED,
                "storage-a",
                java.util.EnumSet.of(WorkspaceProjectionSlice.STORAGE),
                "storage_only_authority_guard"));

        assertEquals("localized_incremental_projection_not_enabled", result.diagnostics().fullProjectionReason());
        assertEquals(17, card(result.viewModel(), "minecraft:oak_planks").totalCount());
        assertEquals(5, chip(result.viewModel(), "storage-a").contents().get(0).count());
    }

    @Test
    void simpleCarriedAndStorageInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity stone = new ItemIdentity("minecraft:stone", ItemComparisonMode.ITEM_ID, "");
        WorkflowDomainSnapshot workflow = workflowWithHomes(homeMap(stone));
        WorkspaceProjectionRequest seed = storageRequest(
                authority(List.of(
                        stack(0, "minecraft:stone", 16),
                        stack(1, "minecraft:dirt", 4))),
                workflow,
                storageIndex(List.of(
                        displayStorageEntry("storage-b", 2, "minecraft:dirt", 9)), 1L));
        WorkspaceProjectionRequest changed = storageRequest(
                authority(List.of(
                        stack(0, "minecraft:stone", 8),
                        stack(1, "minecraft:dirt", 4))),
                workflow,
                storageIndex(List.of(
                        displayStorageEntry("storage-a", 1, "minecraft:stone", 8),
                        displayStorageEntry("storage-b", 2, "minecraft:dirt", 9)), 2L));
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED,
                Set.of(stone),
                Set.of("storage-a"),
                Set.of("materials"),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.STORAGE),
                false,
                "simple_carried_to_storage");

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos());
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertEquals(8, card(result.viewModel(), "minecraft:stone").totalCount());
        assertEquals("materials", card(result.viewModel(), "minecraft:stone").islandId());
        assertEquals(8, chip(result.viewModel(), "storage-a").contents().get(0).count());
        assertEquals(9, chip(result.viewModel(), "storage-b").contents().get(0).count());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleProximateStorageTakeInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity stone = new ItemIdentity("minecraft:stone", ItemComparisonMode.ITEM_ID, "");
        UUID storageId = UUID.fromString("00000000-0000-0000-0000-000000000101");
        ClaimedChest chest = claimedChest(storageId);
        ClaimedChestMap claimedChests = new ClaimedChestMap(List.of(chest));
        WorkflowDomainSnapshot workflow = workflowWithHomesAndTargetsAndChests(
                homeMap(stone),
                Map.of(),
                Map.of(stone, 32),
                claimedChests);
        WorkspaceProjectionRequest seed = storageRequestWithResolverAndProximateIds(
                InventoryAuthoritySnapshot.empty(),
                workflow,
                storageIndex(List.of(claimedStorageEntry(storageId, "minecraft:stone", 9, true)), 1L),
                Set.of(storageId.toString()));
        WorkspaceProjectionRequest changed = storageRequestWithResolverAndProximateIds(
                authority("minecraft:stone", 9),
                workflow,
                storageIndex(List.of(claimedEmptyStorageEntry(storageId, true)), 2L),
                Set.of(storageId.toString()));
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED,
                Set.of(stone),
                Set.of(storageId.toString()),
                Set.of("materials"),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.STORAGE,
                        WorkspaceProjectionSlice.WAYFINDING,
                        WorkspaceProjectionSlice.WORKFLOW),
                false,
                "simple_proximate_storage_take");

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.AtlasItem card = card(result.viewModel(), "minecraft:stone");

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertTrue(card.carried());
        assertFalse(card.ghost());
        assertEquals(9, card.totalCount());
        assertEquals(0, card.proximateCount());
        assertTrue(card.presence().isEmpty());
        assertEquals(32, card.wantedCount());
        assertTrue(result.viewModel().wayfindingTargets().isEmpty());
        assertTrue(chip(result.viewModel(), storageId.toString()).contents().isEmpty());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void identityMemoSurvivesAcrossStructuralMisses() {
        WorkspaceProjectionSessionCache cache = new WorkspaceProjectionSessionCache();
        String patchouliFingerprint = "{Patchouli:Book=>slot:test_book,Damage=12}";

        WorkspaceProjectionResult first = cache.project(
                request(
                        authority("patchouli:guide_book", patchouliFingerprint, 1),
                        "ready",
                        "",
                        0,
                        0,
                        "a",
                        RemoteStorageDetailIntent.SEARCH));
        WorkspaceProjectionResult second = cache.project(
                request(
                        authority("patchouli:guide_book", patchouliFingerprint, 1),
                        "ready",
                        "",
                        0,
                        0,
                        "b",
                        RemoteStorageDetailIntent.SEARCH));

        assertFalse(first.structuralCacheHit());
        assertFalse(second.structuralCacheHit());
        assertTrue(
                second.identityMemoStats().createHits() > first.identityMemoStats().createHits()
                        || second.identityMemoStats().normalizeHits() > first.identityMemoStats().normalizeHits(),
                "expected identity memo hits after a second projection over the same component fingerprint");
    }

    @Test
    void invalidationDiagnosticsExplainFullFallbackAndFactCounts() {
        WorkspaceProjectionSessionCache cache = new WorkspaceProjectionSessionCache();
        ItemIdentity stone = new ItemIdentity("minecraft:stone", ItemComparisonMode.ITEM_ID, "");
        WorkspaceInvalidation invalidation = WorkspaceInvalidation.identityLocal(
                WorkspaceInvalidation.Reason.CARRIED_REVISION_CHANGED,
                stone,
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.HOTBAR),
                "carried_revision_changed_not_localized");

        WorkspaceProjectionResult result = cache.project(
                request(authority("minecraft:stone", 16), "ready", "", 0, 0, ""),
                invalidation);

        assertFalse(result.structuralCacheHit());
        assertEquals(1, result.diagnostics().invalidations().invalidationCount());
        assertEquals(1, result.diagnostics().invalidations().identities().size());
        assertTrue(result.diagnostics().invalidations().requiresFullProjection());
        assertTrue(result.diagnostics().fullProjectionReason().contains("carried_revision_changed_not_localized"));
        assertTrue(result.diagnostics().projectionFactsUpdated() > 0);
        assertEquals(0, result.diagnostics().projectionFactsReused());
    }

    @Test
    void structuralHitReportsReusedProjectionFacts() {
        WorkspaceProjectionSessionCache cache = new WorkspaceProjectionSessionCache();
        WorkspaceProjectionRequest request = request(authority("minecraft:stone", 16), "ready", "", 0, 0, "");

        cache.project(request, WorkspaceInvalidation.full(
                WorkspaceInvalidation.Reason.SESSION_OPEN,
                "test_seed"));
        WorkspaceProjectionResult result = cache.project(request, WorkspaceInvalidation.frame(
                WorkspaceInvalidation.Reason.COMMAND_OUTCOME,
                "status_only"));

        assertTrue(result.structuralCacheHit());
        assertEquals(0, result.diagnostics().projectionFactsUpdated());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
        assertEquals(1, result.diagnostics().invalidations().invalidationCount());
        assertFalse(result.diagnostics().invalidations().requiresFullProjection());
    }

    @Test
    void searchQueryChangeWithoutRemoteStorageSkipsFullProjection() {
        WorkspaceProjectionSessionCache cache = new WorkspaceProjectionSessionCache();
        cache.project(request(authority("minecraft:stone", 16), "ready", "", 0, 0, ""));

        WorkspaceProjectionResult result = cache.project(
                request(authority("minecraft:stone", 16), "search updated", "query=stone", 0, 0, "stone"),
                new WorkspaceInvalidation(
                        WorkspaceInvalidation.Reason.SEARCH_QUERY_CHANGED,
                        Set.of(),
                        Set.of(),
                        Set.of(),
                        java.util.EnumSet.of(
                                WorkspaceProjectionSlice.CARD,
                                WorkspaceProjectionSlice.SECTION,
                                WorkspaceProjectionSlice.FRAME),
                        false,
                        "remote_search_query_changed"));

        assertFalse(result.diagnostics().invalidations().requiresFullProjection());
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertEquals(0L, result.diagnostics().timing().projectNanos());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void hotbarFrameOnlyInvalidationSkipsFullProjectionAndMatchesOracle() {
        WorkspaceProjectionRequest seed = request(authority("minecraft:stone", 16), "ready", "", 0, 0, "");
        WorkspaceProjectionRequest changed = request(
                withCursor(authority("minecraft:stone", 16), new ItemStack("minecraft:dirt", 1, 64)),
                "cursor changed",
                "cursor=held",
                0,
                1,
                "");
        WorkspaceInvalidation invalidation = WorkspaceInvalidation.hotbarFrame(
                WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED,
                "cursor_only_hotbar_frame");

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();

        assertTrue(parity.matches(), parityDebug(parity));
        assertFalse(result.structuralCacheHit());
        assertEquals(0L, result.diagnostics().timing().projectNanos());
        assertFalse(result.diagnostics().invalidations().requiresFullProjection());
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertTrue(result.diagnostics().projectionFactsUpdated() > 0);
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
        assertEquals(1, result.viewModel().selectedQuickAccessSlot());
    }

    @Test
    void simpleCarriedIdentityCountInvalidationSkipsFullProjectionAndMatchesOracle() {
        WorkspaceProjectionRequest seed = request(authority(List.of(
                stack(0, "minecraft:stone", 16),
                stack(1, "minecraft:dirt", 4))), "ready", "", 0, 0, "");
        WorkspaceProjectionRequest changed = request(authority(List.of(
                stack(0, "minecraft:stone", 17),
                stack(1, "minecraft:dirt", 4))), "ready", "", 0, 0, "");

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(WorkspaceInvalidation.localizedIdentity(
                        WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED,
                        new ItemIdentity("minecraft:stone", ItemComparisonMode.ITEM_ID, ""),
                        java.util.EnumSet.of(WorkspaceProjectionSlice.CARD, WorkspaceProjectionSlice.SECTION),
                        "simple_carried_identity_count")));
        WorkspaceProjectionResult result = parity.invalidated();

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos());
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertFalse(result.diagnostics().invalidations().requiresFullProjection());
        assertEquals(17, card(result.viewModel(), "minecraft:stone").totalCount());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleCarriedContainerIdentityInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity backpack = new ItemIdentity("sophisticatedbackpacks:backpack", ItemComparisonMode.ITEM_ID, "");
        WorkspaceProjectionRequest seed = requestWithContainerResolver(
                authority("sophisticatedbackpacks:backpack", 1),
                containerResolver(backpack, 4, 9));
        WorkspaceProjectionRequest changed = requestWithContainerResolver(
                authority("sophisticatedbackpacks:backpack", 2),
                containerResolver(backpack, 4, 9));

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(WorkspaceInvalidation.localizedIdentity(
                        WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED,
                        backpack,
                        java.util.EnumSet.of(WorkspaceProjectionSlice.CARD, WorkspaceProjectionSlice.SECTION),
                        "simple_carried_container_identity_count")));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.AtlasItem card = card(result.viewModel(), "sophisticatedbackpacks:backpack");

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos());
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertEquals(2, card.totalCount());
        assertTrue(card.isCarriedContainer());
        assertEquals(4, card.containerFreeSlotCount());
        assertEquals(9, card.containerSlotCapacity());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleCarriedContainerMetadataInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity backpack = new ItemIdentity("sophisticatedbackpacks:backpack", ItemComparisonMode.ITEM_ID, "");
        WorkspaceProjectionRequest seed = requestWithContainerResolver(
                authority("sophisticatedbackpacks:backpack", 1),
                containerResolver(backpack, 4, 9));
        WorkspaceProjectionRequest changed = requestWithContainerResolver(
                authority("sophisticatedbackpacks:backpack", 1),
                containerResolver(backpack, 3, 9));

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(WorkspaceInvalidation.localizedIdentity(
                        WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED,
                        backpack,
                        java.util.EnumSet.of(WorkspaceProjectionSlice.CARD, WorkspaceProjectionSlice.SECTION),
                        "simple_carried_container_metadata")));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.AtlasItem card = card(result.viewModel(), "sophisticatedbackpacks:backpack");

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos());
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertEquals(1, card.totalCount());
        assertTrue(card.isCarriedContainer());
        assertEquals(3, card.containerFreeSlotCount());
        assertEquals(9, card.containerSlotCapacity());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleCarriedIdentityAcquireInvalidationSkipsFullProjectionAndMatchesOracle() {
        WorkspaceProjectionRequest seed = request(authority(List.of(
                stack(0, "minecraft:stone", 16))), "ready", "", 0, 0, "");
        WorkspaceProjectionRequest changed = request(authority(List.of(
                stack(0, "minecraft:stone", 16),
                stack(1, "minecraft:dirt", 4))), "ready", "", 0, 0, "");

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(WorkspaceInvalidation.localizedIdentity(
                        WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED,
                        new ItemIdentity("minecraft:dirt", ItemComparisonMode.ITEM_ID, ""),
                        java.util.EnumSet.of(WorkspaceProjectionSlice.CARD, WorkspaceProjectionSlice.SECTION),
                        "simple_carried_identity_acquire")));
        WorkspaceProjectionResult result = parity.invalidated();

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos());
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertEquals(4, card(result.viewModel(), "minecraft:dirt").totalCount());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleCarriedIdentityRemovalInvalidationSkipsFullProjectionAndMatchesOracle() {
        WorkspaceProjectionRequest seed = request(authority(List.of(
                stack(0, "minecraft:stone", 16),
                stack(1, "minecraft:dirt", 4))), "ready", "", 0, 0, "");
        WorkspaceProjectionRequest changed = request(authority(List.of(
                stack(0, "minecraft:stone", 16))), "ready", "", 0, 0, "");

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(WorkspaceInvalidation.localizedIdentity(
                        WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED,
                        new ItemIdentity("minecraft:dirt", ItemComparisonMode.ITEM_ID, ""),
                        java.util.EnumSet.of(WorkspaceProjectionSlice.CARD, WorkspaceProjectionSlice.SECTION),
                        "simple_carried_identity_remove")));
        WorkspaceProjectionResult result = parity.invalidated();

        assertTrue(parity.matches());
        assertEquals(0L, result.diagnostics().timing().projectNanos());
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertFalse(hasCard(result.viewModel(), "minecraft:dirt"));
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void homedCarriedIdentityCountInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity stone = new ItemIdentity("minecraft:stone", ItemComparisonMode.ITEM_ID, "");
        ItemIdentity dirt = new ItemIdentity("minecraft:dirt", ItemComparisonMode.ITEM_ID, "");
        WorkflowDomainSnapshot workflow = workflowWithHomes(homeMap(stone, dirt));
        WorkspaceProjectionRequest seed = request(authority(List.of(
                stack(0, "minecraft:stone", 16),
                stack(1, "minecraft:dirt", 4))), workflow, "ready", "", 0, 0, "");
        WorkspaceProjectionRequest changed = request(authority(List.of(
                stack(0, "minecraft:stone", 17),
                stack(1, "minecraft:dirt", 4))), workflow, "ready", "", 0, 0, "");

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(WorkspaceInvalidation.localizedIdentity(
                        WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED,
                        stone,
                        java.util.EnumSet.of(WorkspaceProjectionSlice.CARD, WorkspaceProjectionSlice.SECTION),
                        "homed_carried_identity_count")));
        WorkspaceProjectionResult result = parity.invalidated();

        assertTrue(parity.matches());
        assertEquals(0L, result.diagnostics().timing().projectNanos());
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertEquals("materials", card(result.viewModel(), "minecraft:stone").islandId());
        assertEquals(17, card(result.viewModel(), "minecraft:stone").totalCount());
        assertEquals(2, result.viewModel().island("materials").carriedCount());
    }

    @Test
    void homedCarriedIdentityCountWithStorageContextSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity stone = new ItemIdentity("minecraft:stone", ItemComparisonMode.ITEM_ID, "");
        ItemIdentity dirt = new ItemIdentity("minecraft:dirt", ItemComparisonMode.ITEM_ID, "");
        WorkflowDomainSnapshot workflow = workflowWithHomes(homeMap(stone, dirt));
        WorkspaceStorageIndex index = storageIndex(List.of(
                displayStorageEntry("storage-a", 1, "minecraft:oak_planks", 4)), 1L);
        WorkspaceProjectionRequest seed = storageRequest(
                authority(List.of(
                        stack(0, "minecraft:stone", 16),
                        stack(1, "minecraft:dirt", 4))),
                workflow,
                index);
        WorkspaceProjectionRequest changed = storageRequest(
                authority(List.of(
                        stack(0, "minecraft:stone", 17),
                        stack(1, "minecraft:dirt", 4))),
                workflow,
                index);

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(WorkspaceInvalidation.localizedIdentity(
                        WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED,
                        stone,
                        java.util.EnumSet.of(WorkspaceProjectionSlice.CARD, WorkspaceProjectionSlice.SECTION),
                        "homed_carried_identity_count_with_storage_context")));
        WorkspaceProjectionResult result = parity.invalidated();

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertEquals("materials", card(result.viewModel(), "minecraft:stone").islandId());
        assertEquals(17, card(result.viewModel(), "minecraft:stone").totalCount());
        assertEquals(2, result.viewModel().island("materials").carriedCount());
        assertEquals(4, chip(result.viewModel(), "storage-a").contents().get(0).count());
        assertTrue(result.diagnostics().storageProjectionStats().reusedStorageChips() > 0);
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void homedCarriedIdentityAcquireWithStorageContextSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity stone = new ItemIdentity("minecraft:stone", ItemComparisonMode.ITEM_ID, "");
        ItemIdentity dirt = new ItemIdentity("minecraft:dirt", ItemComparisonMode.ITEM_ID, "");
        WorkflowDomainSnapshot workflow = workflowWithHomes(homeMap(stone, dirt));
        WorkspaceStorageIndex index = storageIndex(List.of(
                displayStorageEntry("storage-a", 1, "minecraft:oak_planks", 4)), 1L);
        WorkspaceProjectionRequest seed = storageRequest(
                authority(List.of(
                        stack(0, "minecraft:stone", 16))),
                workflow,
                index);
        WorkspaceProjectionRequest changed = storageRequest(
                authority(List.of(
                        stack(0, "minecraft:stone", 16),
                        stack(1, "minecraft:dirt", 4))),
                workflow,
                index);

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(WorkspaceInvalidation.localizedIdentity(
                        WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED,
                        dirt,
                        java.util.EnumSet.of(WorkspaceProjectionSlice.CARD, WorkspaceProjectionSlice.SECTION),
                        "homed_carried_identity_acquire_with_storage_context")));
        WorkspaceProjectionResult result = parity.invalidated();

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertEquals("materials", card(result.viewModel(), "minecraft:dirt").islandId());
        assertEquals(4, card(result.viewModel(), "minecraft:dirt").totalCount());
        assertEquals(2, result.viewModel().island("materials").carriedCount());
        assertEquals(4, chip(result.viewModel(), "storage-a").contents().get(0).count());
        assertTrue(result.diagnostics().storageProjectionStats().reusedStorageChips() > 0);
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void homedCarriedIdentityRemovalWithStorageContextSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity stone = new ItemIdentity("minecraft:stone", ItemComparisonMode.ITEM_ID, "");
        ItemIdentity dirt = new ItemIdentity("minecraft:dirt", ItemComparisonMode.ITEM_ID, "");
        WorkflowDomainSnapshot workflow = workflowWithHomes(homeMap(stone, dirt));
        WorkspaceStorageIndex index = storageIndex(List.of(
                displayStorageEntry("storage-a", 1, "minecraft:oak_planks", 4)), 1L);
        WorkspaceProjectionRequest seed = storageRequest(
                authority(List.of(
                        stack(0, "minecraft:stone", 16),
                        stack(1, "minecraft:dirt", 4))),
                workflow,
                index);
        WorkspaceProjectionRequest changed = storageRequest(
                authority(List.of(
                        stack(0, "minecraft:stone", 16))),
                workflow,
                index);

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(WorkspaceInvalidation.localizedIdentity(
                        WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED,
                        dirt,
                        java.util.EnumSet.of(WorkspaceProjectionSlice.CARD, WorkspaceProjectionSlice.SECTION),
                        "homed_carried_identity_remove_with_storage_context")));
        WorkspaceProjectionResult result = parity.invalidated();

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertFalse(hasCard(result.viewModel(), "minecraft:dirt"));
        assertEquals(1, result.viewModel().island("materials").carriedCount());
        assertEquals(4, chip(result.viewModel(), "storage-a").contents().get(0).count());
        assertTrue(result.diagnostics().storageProjectionStats().reusedStorageChips() > 0);
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void homedCarriedIdentityAcquireInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity stone = new ItemIdentity("minecraft:stone", ItemComparisonMode.ITEM_ID, "");
        ItemIdentity dirt = new ItemIdentity("minecraft:dirt", ItemComparisonMode.ITEM_ID, "");
        WorkflowDomainSnapshot workflow = workflowWithHomes(homeMap(stone, dirt));
        WorkspaceProjectionRequest seed = request(authority(List.of(
                stack(0, "minecraft:stone", 16))), workflow, "ready", "", 0, 0, "");
        WorkspaceProjectionRequest changed = request(authority(List.of(
                stack(0, "minecraft:stone", 16),
                stack(1, "minecraft:dirt", 4))), workflow, "ready", "", 0, 0, "");

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(WorkspaceInvalidation.localizedIdentity(
                        WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED,
                        dirt,
                        java.util.EnumSet.of(WorkspaceProjectionSlice.CARD, WorkspaceProjectionSlice.SECTION),
                        "homed_carried_identity_acquire")));
        WorkspaceProjectionResult result = parity.invalidated();

        assertTrue(parity.matches());
        assertEquals(0L, result.diagnostics().timing().projectNanos());
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertEquals("materials", card(result.viewModel(), "minecraft:dirt").islandId());
        assertEquals(2, result.viewModel().island("materials").carriedCount());
    }

    @Test
    void homedCarriedIdentityRemovalInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity stone = new ItemIdentity("minecraft:stone", ItemComparisonMode.ITEM_ID, "");
        ItemIdentity dirt = new ItemIdentity("minecraft:dirt", ItemComparisonMode.ITEM_ID, "");
        WorkflowDomainSnapshot workflow = workflowWithHomes(homeMap(stone, dirt));
        WorkspaceProjectionRequest seed = request(authority(List.of(
                stack(0, "minecraft:stone", 16),
                stack(1, "minecraft:dirt", 4))), workflow, "ready", "", 0, 0, "");
        WorkspaceProjectionRequest changed = request(authority(List.of(
                stack(0, "minecraft:stone", 16))), workflow, "ready", "", 0, 0, "");

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(WorkspaceInvalidation.localizedIdentity(
                        WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED,
                        dirt,
                        java.util.EnumSet.of(WorkspaceProjectionSlice.CARD, WorkspaceProjectionSlice.SECTION),
                        "homed_carried_identity_remove")));
        WorkspaceProjectionResult result = parity.invalidated();

        assertTrue(parity.matches());
        assertEquals(0L, result.diagnostics().timing().projectNanos());
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertFalse(hasCard(result.viewModel(), "minecraft:dirt"));
        assertEquals(1, result.viewModel().island("materials").carriedCount());
    }

    @Test
    void simplePlayerDesiredTargetInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity stone = new ItemIdentity("minecraft:stone", ItemComparisonMode.ITEM_ID, "");
        VisualHomeMap homes = homeMap(stone);
        WorkspaceProjectionRequest seed = request(
                authority("minecraft:stone", 16),
                workflowWithHomes(homes),
                "ready",
                "",
                0,
                0,
                "");
        WorkspaceProjectionRequest changed = request(
                authority("minecraft:stone", 16),
                workflowWithHomesAndTargets(homes, Map.of(stone, 32), Map.of()),
                "ready",
                "",
                0,
                0,
                "");

        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.COMMAND_OUTCOME,
                Set.of(stone),
                Set.of(),
                Set.of(),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.WORKFLOW),
                false,
                "simple_player_desired_target");
        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos());
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertEquals(32, card(result.viewModel(), "minecraft:stone").desiredCount());
        assertEquals(0, card(result.viewModel(), "minecraft:stone").wantedCount());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simplePlayerWantedTargetInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity stone = new ItemIdentity("minecraft:stone", ItemComparisonMode.ITEM_ID, "");
        VisualHomeMap homes = homeMap(stone);
        WorkspaceProjectionRequest seed = request(
                authority("minecraft:stone", 16),
                workflowWithHomes(homes),
                "ready",
                "",
                0,
                0,
                "");
        WorkspaceProjectionRequest changed = request(
                authority("minecraft:stone", 16),
                workflowWithHomesAndTargets(homes, Map.of(), Map.of(stone, 32)),
                "ready",
                "",
                0,
                0,
                "");

        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.COMMAND_OUTCOME,
                Set.of(stone),
                Set.of(),
                Set.of(),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.WORKFLOW),
                false,
                "simple_player_wanted_target");
        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos());
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertEquals(0, card(result.viewModel(), "minecraft:stone").desiredCount());
        assertEquals(32, card(result.viewModel(), "minecraft:stone").wantedCount());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleSatisfiedPlayerWantedTargetInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity stone = new ItemIdentity("minecraft:stone", ItemComparisonMode.ITEM_ID, "");
        VisualHomeMap homes = homeMap(stone);
        WorkflowDomainSnapshot workflow = workflowWithHomesAndTargets(homes, Map.of(), Map.of(stone, 32));
        WorkspaceProjectionRequest seed = request(
                authority("minecraft:stone", 16),
                workflow,
                "ready",
                "",
                0,
                0,
                "");
        WorkspaceProjectionRequest changed = request(
                authority("minecraft:stone", 32),
                workflow,
                "ready",
                "",
                0,
                0,
                "");

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(WorkspaceInvalidation.localizedIdentity(
                        WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED,
                        stone,
                        java.util.EnumSet.of(
                                WorkspaceProjectionSlice.CARD,
                                WorkspaceProjectionSlice.SECTION,
                                WorkspaceProjectionSlice.WORKFLOW),
                        "simple_player_wanted_target_satisfied")));
        WorkspaceProjectionResult result = parity.invalidated();

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos());
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertEquals(32, card(result.viewModel(), "minecraft:stone").totalCount());
        assertEquals(0, card(result.viewModel(), "minecraft:stone").wantedCount());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simplePlayerTargetRemovalInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity stone = new ItemIdentity("minecraft:stone", ItemComparisonMode.ITEM_ID, "");
        VisualHomeMap homes = homeMap(stone);
        WorkspaceProjectionRequest seed = request(
                authority("minecraft:stone", 16),
                workflowWithHomesAndTargets(homes, Map.of(stone, 32), Map.of()),
                "ready",
                "",
                0,
                0,
                "");
        WorkspaceProjectionRequest changed = request(
                authority("minecraft:stone", 16),
                workflowWithHomes(homes),
                "ready",
                "",
                0,
                0,
                "");

        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.COMMAND_OUTCOME,
                Set.of(stone),
                Set.of(),
                Set.of(),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.WORKFLOW),
                false,
                "simple_player_target_remove");
        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos());
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertEquals(0, card(result.viewModel(), "minecraft:stone").desiredCount());
        assertEquals(0, card(result.viewModel(), "minecraft:stone").wantedCount());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleCarriedJunkTagInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity stone = new ItemIdentity("minecraft:stone", ItemComparisonMode.ITEM_ID, "");
        VisualHomeMap homes = homeMap(stone);
        WorkspaceProjectionRequest seed = request(
                authority("minecraft:stone", 16),
                workflowWithHomes(homes),
                "ready",
                "",
                0,
                0,
                "");
        WorkspaceProjectionRequest changed = request(
                authority("minecraft:stone", 16),
                workflowWithJunk(homes, Set.of(stone)),
                "ready",
                "",
                0,
                0,
                "");
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.COMMAND_OUTCOME,
                Set.of(stone),
                Set.of(),
                Set.of(),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.WORKFLOW),
                false,
                "simple_carried_junk_tag");

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertTrue(card(result.viewModel(), "minecraft:stone").junk());
        assertEquals("materials", card(result.viewModel(), "minecraft:stone").islandId());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleCarriedJunkTagRemovalInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity stone = new ItemIdentity("minecraft:stone", ItemComparisonMode.ITEM_ID, "");
        VisualHomeMap homes = homeMap(stone);
        WorkspaceProjectionRequest seed = request(
                authority("minecraft:stone", 16),
                workflowWithJunk(homes, Set.of(stone)),
                "ready",
                "",
                0,
                0,
                "");
        WorkspaceProjectionRequest changed = request(
                authority("minecraft:stone", 16),
                workflowWithHomes(homes),
                "ready",
                "",
                0,
                0,
                "");
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.COMMAND_OUTCOME,
                Set.of(stone),
                Set.of(),
                Set.of(),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.WORKFLOW),
                false,
                "simple_carried_junk_tag_remove");

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertFalse(card(result.viewModel(), "minecraft:stone").junk());
        assertEquals("materials", card(result.viewModel(), "minecraft:stone").islandId());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simplePlayerDesiredTargetGhostInvalidationSkipsFullProjectionAndMatchesOracle() {
        SlotWorkspaceViewModel.setGhostStackResolver(itemId -> new ItemStack(itemId, 1, 64));
        ItemIdentity stone = new ItemIdentity("minecraft:stone", ItemComparisonMode.ITEM_ID, "");
        VisualHomeMap homes = homeMap(stone);
        WorkspaceProjectionRequest seed = request(
                authority(List.of()),
                workflowWithHomes(homes),
                "ready",
                "",
                0,
                0,
                "");
        WorkspaceProjectionRequest changed = request(
                authority(List.of()),
                workflowWithHomesAndTargets(homes, Map.of(stone, 32), Map.of()),
                "ready",
                "",
                0,
                0,
                "");

        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.COMMAND_OUTCOME,
                Set.of(stone),
                Set.of(),
                Set.of(),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.WORKFLOW),
                false,
                "simple_player_desired_target_ghost");
        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.AtlasItem ghost = card(result.viewModel(), "minecraft:stone");

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos());
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertFalse(ghost.carried());
        assertTrue(ghost.ghost());
        assertEquals("materials", ghost.islandId());
        assertEquals(32, ghost.desiredCount());
        assertEquals(1, result.viewModel().contextualSuggestionLanes().size());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simplePlayerWantedTargetGhostInvalidationSkipsFullProjectionAndMatchesOracle() {
        SlotWorkspaceViewModel.setGhostStackResolver(itemId -> new ItemStack(itemId, 1, 64));
        ItemIdentity stone = new ItemIdentity("minecraft:stone", ItemComparisonMode.ITEM_ID, "");
        WorkspaceProjectionRequest seed = request(
                authority(List.of()),
                workflowWithHomes(VisualHomeMap.empty()),
                "ready",
                "",
                0,
                0,
                "");
        WorkspaceProjectionRequest changed = request(
                authority(List.of()),
                workflowWithHomesAndTargets(VisualHomeMap.empty(), Map.of(), Map.of(stone, 32)),
                "ready",
                "",
                0,
                0,
                "");

        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.COMMAND_OUTCOME,
                Set.of(stone),
                Set.of(),
                Set.of(),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.WORKFLOW),
                false,
                "simple_player_wanted_target_ghost");
        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.AtlasItem ghost = card(result.viewModel(), "minecraft:stone");

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos());
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertFalse(ghost.carried());
        assertTrue(ghost.ghost());
        assertEquals(SlotWorkspaceAtlasLayout.ISLAND_MISC, ghost.islandId());
        assertEquals(32, ghost.wantedCount());
        assertEquals(1, result.viewModel().islands().size());
        assertEquals(SlotWorkspaceAtlasLayout.ISLAND_MISC, result.viewModel().islands().get(0).islandId());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simplePlayerTargetGhostRemovalInvalidationSkipsFullProjectionAndMatchesOracle() {
        SlotWorkspaceViewModel.setGhostStackResolver(itemId -> new ItemStack(itemId, 1, 64));
        ItemIdentity stone = new ItemIdentity("minecraft:stone", ItemComparisonMode.ITEM_ID, "");
        WorkspaceProjectionRequest seed = request(
                authority(List.of()),
                workflowWithHomesAndTargets(VisualHomeMap.empty(), Map.of(stone, 32), Map.of()),
                "ready",
                "",
                0,
                0,
                "");
        WorkspaceProjectionRequest changed = request(
                authority(List.of()),
                workflowWithHomes(VisualHomeMap.empty()),
                "ready",
                "",
                0,
                0,
                "");

        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.COMMAND_OUTCOME,
                Set.of(stone),
                Set.of(),
                Set.of(),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.WORKFLOW),
                false,
                "simple_player_target_ghost_remove");
        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos());
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertFalse(hasCard(result.viewModel(), "minecraft:stone"));
        assertTrue(result.viewModel().contextualSuggestionLanes().isEmpty());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleCraftRunAddRecipeInvalidationSkipsFullProjectionAndMatchesOracle() {
        SlotWorkspaceViewModel.setGhostStackResolver(itemId -> new ItemStack(itemId, 1, 64));
        ItemIdentity redstone = new ItemIdentity("minecraft:redstone", ItemComparisonMode.ITEM_ID, "");
        CraftRunState craftRun = craftRun(redstone, 3, 1);
        WorkspaceProjectionRequest seed = request(
                authority(List.of()),
                workflowWithHomes(homeMap(redstone)),
                "ready",
                "",
                0,
                0,
                "");
        WorkspaceProjectionRequest changed = request(
                authority(List.of()),
                workflowWithHomesAndCraftRun(homeMap(redstone), craftRun),
                "ready",
                "",
                0,
                0,
                "");
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.WORKFLOW_SEQUENCE_CHANGED,
                Set.of(redstone),
                Set.of(),
                Set.of("materials"),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.WORKFLOW,
                        WorkspaceProjectionSlice.CONTEXTUAL),
                false,
                "simple_craft_run_recipe_add");

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.AtlasItem ghost = card(result.viewModel(), "minecraft:redstone");

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertFalse(ghost.carried());
        assertTrue(ghost.ghost());
        assertEquals("materials", ghost.islandId());
        assertEquals(3, ghost.wantedCount());
        assertEquals(craftRun, result.viewModel().craftRun());
        assertEquals(1, result.viewModel().contextualSuggestionLanes().size());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleCraftRunRemoveRecipeInvalidationSkipsFullProjectionAndMatchesOracle() {
        SlotWorkspaceViewModel.setGhostStackResolver(itemId -> new ItemStack(itemId, 1, 64));
        ItemIdentity redstone = new ItemIdentity("minecraft:redstone", ItemComparisonMode.ITEM_ID, "");
        CraftRunState craftRun = craftRun(redstone, 3, 1);
        WorkspaceProjectionRequest seed = request(
                authority(List.of()),
                workflowWithHomesAndCraftRun(homeMap(redstone), craftRun),
                "ready",
                "",
                0,
                0,
                "");
        WorkspaceProjectionRequest changed = request(
                authority(List.of()),
                workflowWithHomes(homeMap(redstone)),
                "ready",
                "",
                0,
                0,
                "");
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.WORKFLOW_SEQUENCE_CHANGED,
                Set.of(redstone),
                Set.of(),
                Set.of("materials"),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.WORKFLOW,
                        WorkspaceProjectionSlice.CONTEXTUAL),
                false,
                "simple_craft_run_recipe_remove");

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertFalse(hasCard(result.viewModel(), "minecraft:redstone"));
        assertEquals(CraftRunState.empty(), result.viewModel().craftRun());
        assertTrue(result.viewModel().contextualSuggestionLanes().isEmpty());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleCraftRunAddRecipeWithClaimedStorageInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity redstone = new ItemIdentity("minecraft:redstone", ItemComparisonMode.ITEM_ID, "");
        UUID storageId = UUID.fromString("00000000-0000-0000-0000-000000000087");
        ClaimedChest chest = claimedChest(storageId);
        ClaimedChestMap claimedChests = new ClaimedChestMap(List.of(chest));
        CraftRunState craftRun = craftRun(redstone, 3, 1);
        WorkspaceStorageIndex index = storageIndex(
                List.of(claimedStorageEntry(storageId, "minecraft:redstone", 9, true)),
                1L);
        WorkspaceProjectionRequest seed = storageRequestWithResolverAndProximateIds(
                InventoryAuthoritySnapshot.empty(),
                workflowWithHomesCraftRunAndChests(homeMap(redstone), CraftRunState.empty(), claimedChests),
                index,
                Set.of(storageId.toString()));
        WorkspaceProjectionRequest changed = storageRequestWithResolverAndProximateIds(
                InventoryAuthoritySnapshot.empty(),
                workflowWithHomesCraftRunAndChests(homeMap(redstone), craftRun, claimedChests),
                index,
                Set.of(storageId.toString()));
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.WORKFLOW_SEQUENCE_CHANGED,
                Set.of(redstone),
                Set.of(),
                Set.of("materials"),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.WORKFLOW,
                        WorkspaceProjectionSlice.CONTEXTUAL,
                        WorkspaceProjectionSlice.WAYFINDING,
                        WorkspaceProjectionSlice.FRAME),
                false,
                "simple_craft_run_recipe_add_claimed_storage");
        SlotWorkspaceViewModel.setGhostStackResolver(itemId -> new ItemStack(itemId, 1, 64));

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.AtlasItem ghost = card(result.viewModel(), "minecraft:redstone");
        WayfindingTarget target = result.viewModel().wayfindingTargets().get(0);

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertFalse(ghost.carried());
        assertTrue(ghost.ghost());
        assertEquals("materials", ghost.islandId());
        assertEquals(9, ghost.totalCount());
        assertEquals(9, ghost.proximateCount());
        assertTrue(ghost.kitNeeded());
        assertEquals(3, ghost.wantedCount());
        assertEquals(1, ghost.presence().size());
        assertEquals(storageId.toString(), ghost.presence().get(0).storageId());
        assertEquals(1, result.viewModel().wayfindingTargets().size());
        assertEquals(storageId.toString(), target.storageId());
        assertEquals(WayfindingTarget.Scope.KIT, target.scope());
        assertTrue(target.kitMissingIdentities().contains(redstone));
        assertTrue(target.wantedMissingIdentities().contains(redstone));
        assertEquals(9, target.totalMissingCount());
        assertEquals(craftRun, result.viewModel().craftRun());
        assertEquals("cluster-" + storageId, chip(result.viewModel(), storageId.toString()).clusterId());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleCraftRunSelectedAlternativeWithClaimedStorageInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity redstone = new ItemIdentity("minecraft:redstone", ItemComparisonMode.ITEM_ID, "");
        ItemIdentity glowstone = new ItemIdentity("minecraft:glowstone_dust", ItemComparisonMode.ITEM_ID, "");
        ItemIdentity output = new ItemIdentity("minecraft:comparator", ItemComparisonMode.ITEM_ID, "");
        UUID storageId = UUID.fromString("00000000-0000-0000-0000-000000000093");
        ClaimedChest chest = claimedChest(storageId);
        ClaimedChestMap claimedChests = new ClaimedChestMap(List.of(chest));
        CraftRunState craftRun = craftRunWithSelectedAlternative(glowstone, redstone, 3, 1);
        WorkspaceStorageIndex index = storageIndex(
                List.of(claimedStorageEntry(storageId, "minecraft:glowstone_dust", 9, true)),
                1L);
        WorkspaceProjectionRequest seed = storageRequestWithResolverAndProximateIds(
                InventoryAuthoritySnapshot.empty(),
                workflowWithHomesCraftRunAndChests(homeMap(glowstone), CraftRunState.empty(), claimedChests),
                index,
                Set.of(storageId.toString()));
        WorkspaceProjectionRequest changed = storageRequestWithResolverAndProximateIds(
                InventoryAuthoritySnapshot.empty(),
                workflowWithHomesCraftRunAndChests(homeMap(glowstone), craftRun, claimedChests),
                index,
                Set.of(storageId.toString()));
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.WORKFLOW_SEQUENCE_CHANGED,
                Set.of(output, redstone, glowstone),
                Set.of(),
                Set.of("materials"),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.WORKFLOW,
                        WorkspaceProjectionSlice.CONTEXTUAL,
                        WorkspaceProjectionSlice.WAYFINDING,
                        WorkspaceProjectionSlice.FRAME),
                false,
                "simple_craft_run_selected_alternative_claimed_storage");
        SlotWorkspaceViewModel.setGhostStackResolver(itemId -> new ItemStack(itemId, 1, 64));

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.AtlasItem ghost = card(result.viewModel(), "minecraft:glowstone_dust");
        WayfindingTarget target = result.viewModel().wayfindingTargets().get(0);

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertFalse(hasCard(result.viewModel(), "minecraft:redstone"));
        assertFalse(hasCard(result.viewModel(), "minecraft:comparator"));
        assertFalse(ghost.carried());
        assertTrue(ghost.ghost());
        assertEquals("materials", ghost.islandId());
        assertEquals(9, ghost.totalCount());
        assertEquals(9, ghost.proximateCount());
        assertTrue(ghost.kitNeeded());
        assertEquals(3, ghost.wantedCount());
        assertEquals(1, result.viewModel().wayfindingTargets().size());
        assertEquals(storageId.toString(), target.storageId());
        assertEquals(WayfindingTarget.Scope.KIT, target.scope());
        assertTrue(target.kitMissingIdentities().contains(glowstone));
        assertTrue(target.wantedMissingIdentities().contains(glowstone));
        assertEquals(9, target.totalMissingCount());
        assertEquals(craftRun, result.viewModel().craftRun());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleCraftRunUnselectedAlternativesWithoutCarriedPressureSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity redstone = new ItemIdentity("minecraft:redstone", ItemComparisonMode.ITEM_ID, "");
        ItemIdentity glowstone = new ItemIdentity("minecraft:glowstone_dust", ItemComparisonMode.ITEM_ID, "");
        ItemIdentity output = new ItemIdentity("minecraft:comparator", ItemComparisonMode.ITEM_ID, "");
        CraftRunState craftRun = craftRunWithAlternatives(redstone, glowstone, 3, 1);
        WorkspaceProjectionRequest seed = request(
                InventoryAuthoritySnapshot.empty(),
                workflowWithHomes(homeMap(redstone, glowstone)),
                "ready",
                "",
                0,
                0,
                "");
        WorkspaceProjectionRequest changed = request(
                InventoryAuthoritySnapshot.empty(),
                workflowWithHomesAndCraftRun(homeMap(redstone, glowstone), craftRun),
                "ready",
                "",
                0,
                0,
                "");
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.WORKFLOW_SEQUENCE_CHANGED,
                Set.of(output, redstone, glowstone),
                Set.of(),
                Set.of("materials"),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.WORKFLOW,
                        WorkspaceProjectionSlice.CONTEXTUAL),
                false,
                "simple_craft_run_unselected_alternatives_no_pressure");

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertFalse(hasCard(result.viewModel(), "minecraft:redstone"));
        assertFalse(hasCard(result.viewModel(), "minecraft:glowstone_dust"));
        assertFalse(hasCard(result.viewModel(), "minecraft:comparator"));
        assertEquals(craftRun, result.viewModel().craftRun());
        assertTrue(result.viewModel().contextualSuggestionLanes().isEmpty());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleCraftRunCarriedAlternativePressureSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity redstone = new ItemIdentity("minecraft:redstone", ItemComparisonMode.ITEM_ID, "");
        ItemIdentity glowstone = new ItemIdentity("minecraft:glowstone_dust", ItemComparisonMode.ITEM_ID, "");
        ItemIdentity output = new ItemIdentity("minecraft:comparator", ItemComparisonMode.ITEM_ID, "");
        CraftRunState craftRun = craftRunWithAlternatives(redstone, glowstone, 3, 1);
        InventoryAuthoritySnapshot authority = authority(List.of(stack(2, "minecraft:redstone", 1)));
        WorkspaceProjectionRequest seed = request(
                authority,
                workflowWithHomes(homeMap(redstone, glowstone)),
                "ready",
                "",
                0,
                0,
                "");
        WorkspaceProjectionRequest changed = request(
                authority,
                workflowWithHomesAndCraftRun(homeMap(redstone, glowstone), craftRun),
                "ready",
                "",
                0,
                0,
                "");
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.WORKFLOW_SEQUENCE_CHANGED,
                Set.of(output, redstone, glowstone),
                Set.of(),
                Set.of("materials"),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.WORKFLOW,
                        WorkspaceProjectionSlice.CONTEXTUAL),
                false,
                "simple_craft_run_carried_alternative_pressure");

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.AtlasItem card = card(result.viewModel(), "minecraft:redstone");

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertTrue(card.carried());
        assertFalse(card.ghost());
        assertEquals(1, card.totalCount());
        assertEquals(3, card.wantedCount());
        assertFalse(card.kitNeeded());
        assertFalse(hasCard(result.viewModel(), "minecraft:glowstone_dust"));
        assertFalse(hasCard(result.viewModel(), "minecraft:comparator"));
        assertEquals(craftRun, result.viewModel().craftRun());
        assertEquals(1, result.viewModel().contextualSuggestionLanes().size());
        assertEquals("minecraft:redstone", result.viewModel().contextualSuggestionLanes().get(0).items().get(0).identity().itemId());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleCraftRunRemoveRecipeWithClaimedStorageInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity redstone = new ItemIdentity("minecraft:redstone", ItemComparisonMode.ITEM_ID, "");
        UUID storageId = UUID.fromString("00000000-0000-0000-0000-000000000088");
        ClaimedChest chest = claimedChest(storageId);
        ClaimedChestMap claimedChests = new ClaimedChestMap(List.of(chest));
        CraftRunState craftRun = craftRun(redstone, 3, 1);
        WorkspaceStorageIndex index = storageIndex(
                List.of(claimedStorageEntry(storageId, "minecraft:redstone", 9, true)),
                1L);
        WorkspaceProjectionRequest seed = storageRequestWithResolverAndProximateIds(
                InventoryAuthoritySnapshot.empty(),
                workflowWithHomesCraftRunAndChests(homeMap(redstone), craftRun, claimedChests),
                index,
                Set.of(storageId.toString()));
        WorkspaceProjectionRequest changed = storageRequestWithResolverAndProximateIds(
                InventoryAuthoritySnapshot.empty(),
                workflowWithHomesCraftRunAndChests(homeMap(redstone), CraftRunState.empty(), claimedChests),
                index,
                Set.of(storageId.toString()));
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.WORKFLOW_SEQUENCE_CHANGED,
                Set.of(redstone),
                Set.of(),
                Set.of("materials"),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.WORKFLOW,
                        WorkspaceProjectionSlice.CONTEXTUAL,
                        WorkspaceProjectionSlice.WAYFINDING,
                        WorkspaceProjectionSlice.FRAME),
                false,
                "simple_craft_run_recipe_remove_claimed_storage");
        SlotWorkspaceViewModel.setGhostStackResolver(itemId -> new ItemStack(itemId, 1, 64));

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.AtlasItem ghost = card(result.viewModel(), "minecraft:redstone");

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertFalse(ghost.carried());
        assertTrue(ghost.ghost());
        assertEquals(9, ghost.totalCount());
        assertEquals(9, ghost.proximateCount());
        assertFalse(ghost.kitNeeded());
        assertEquals(0, ghost.wantedCount());
        assertEquals(1, ghost.presence().size());
        assertEquals(storageId.toString(), ghost.presence().get(0).storageId());
        assertTrue(result.viewModel().wayfindingTargets().isEmpty());
        assertTrue(result.viewModel().contextualSuggestionLanes().isEmpty());
        assertEquals(CraftRunState.empty(), result.viewModel().craftRun());
        assertEquals("cluster-" + storageId, chip(result.viewModel(), storageId.toString()).clusterId());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleWorkflowActivationInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity hammer = new ItemIdentity("gtceu:steel_mining_hammer", ItemComparisonMode.ITEM_ID, "");
        KitDefinition kit = new KitDefinition(
                "kit-1",
                "Mining",
                List.of(KitPage.empty().withSlot(0, hammer)),
                null);
        WorkflowDomainSnapshot seedWorkflow = workflowWithKitMap(
                VisualHomeMap.empty(),
                new KitMap(List.of(kit), KitActivation.NONE));
        WorkflowDomainSnapshot changedWorkflow = workflowWithKitMap(
                VisualHomeMap.empty(),
                new KitMap(List.of(kit), new KitActivation(kit.id(), 0)));
        WorkspaceProjectionRequest seed = storageRequest(
                InventoryAuthoritySnapshot.empty(),
                seedWorkflow,
                WorkspaceStorageIndex.empty());
        WorkspaceProjectionRequest changed = storageRequest(
                InventoryAuthoritySnapshot.empty(),
                changedWorkflow,
                WorkspaceStorageIndex.empty());
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.WORKFLOW_SEQUENCE_CHANGED,
                Set.of(hammer),
                Set.of(),
                Set.of(),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.WORKFLOW,
                        WorkspaceProjectionSlice.CONTEXTUAL,
                        WorkspaceProjectionSlice.FRAME),
                false,
                "simple_workflow_activate");
        SlotWorkspaceViewModel.setGhostStackResolver(itemId -> new ItemStack(itemId, 1, 64));

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.AtlasItem ghost = card(result.viewModel(), "gtceu:steel_mining_hammer");
        SlotWorkspaceViewModel.KitCard activeKit = result.viewModel().activeKit();

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertTrue(ghost.ghost());
        assertTrue(ghost.kitNeeded());
        assertFalse(ghost.desiredCountFromKit());
        assertEquals(0, ghost.desiredCount());
        assertEquals("kit-1", activeKit.kitId());
        assertTrue(activeKit.active());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleWorkflowMetadataCreateInvalidationSkipsFullProjectionAndMatchesOracle() {
        KitDefinition kit = new KitDefinition(
                "kit-1",
                "Empty",
                List.of(KitPage.empty()),
                null);
        WorkspaceProjectionRequest seed = storageRequest(
                InventoryAuthoritySnapshot.empty(),
                WorkflowDomainSnapshot.empty(),
                WorkspaceStorageIndex.empty());
        WorkspaceProjectionRequest changed = storageRequest(
                InventoryAuthoritySnapshot.empty(),
                workflowWithKitMap(
                        VisualHomeMap.empty(),
                        new KitMap(List.of(kit), new KitActivation(kit.id(), 0))),
                WorkspaceStorageIndex.empty());

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(
                        seed,
                        changed,
                        List.of(workflowMetadataInvalidation("simple_workflow_metadata_create")));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.KitCard activeKit = result.viewModel().activeKit();

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertEquals(1, result.viewModel().kits().size());
        assertEquals("kit-1", activeKit.kitId());
        assertTrue(activeKit.active());
        assertTrue(result.viewModel().atlasItems().isEmpty());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleWorkflowMetadataDeactivationInvalidationSkipsFullProjectionAndMatchesOracle() {
        KitDefinition kit = new KitDefinition(
                "kit-1",
                "Empty",
                List.of(KitPage.empty()),
                null);
        WorkspaceProjectionRequest seed = storageRequest(
                InventoryAuthoritySnapshot.empty(),
                workflowWithKitMap(
                        VisualHomeMap.empty(),
                        new KitMap(List.of(kit), new KitActivation(kit.id(), 0))),
                WorkspaceStorageIndex.empty());
        WorkspaceProjectionRequest changed = storageRequest(
                InventoryAuthoritySnapshot.empty(),
                workflowWithKitMap(
                        VisualHomeMap.empty(),
                        new KitMap(List.of(kit), KitActivation.NONE)),
                WorkspaceStorageIndex.empty());

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(
                        seed,
                        changed,
                        List.of(workflowMetadataInvalidation("simple_workflow_metadata_deactivate")));
        WorkspaceProjectionResult result = parity.invalidated();

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertEquals(1, result.viewModel().kits().size());
        assertNull(result.viewModel().activeKit());
        assertFalse(result.viewModel().kits().get(0).active());
        assertTrue(result.viewModel().atlasItems().isEmpty());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleWorkflowCarriedAcquireInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity hammer = new ItemIdentity("gtceu:steel_mining_hammer", ItemComparisonMode.ITEM_ID, "");
        KitDefinition kit = new KitDefinition(
                "kit-1",
                "Mining",
                List.of(KitPage.empty().withSlot(0, hammer)),
                null);
        WorkflowDomainSnapshot workflow = workflowWithKitMap(
                VisualHomeMap.empty(),
                new KitMap(List.of(kit), new KitActivation(kit.id(), 0, Set.of())));
        WorkspaceProjectionRequest seed = request(
                InventoryAuthoritySnapshot.empty(),
                workflow,
                "ready",
                "",
                0,
                0,
                "");
        WorkspaceProjectionRequest changed = request(
                authority("gtceu:steel_mining_hammer", 1),
                workflow,
                "ready",
                "",
                0,
                0,
                "");
        WorkspaceInvalidation invalidation = WorkspaceInvalidation.localizedIdentity(
                WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED,
                hammer,
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.WORKFLOW,
                        WorkspaceProjectionSlice.CONTEXTUAL,
                        WorkspaceProjectionSlice.WAYFINDING),
                "simple_workflow_carried_acquire");
        SlotWorkspaceViewModel.setGhostStackResolver(itemId -> new ItemStack(itemId, 1, 64));

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.AtlasItem hammerCard = card(result.viewModel(), "gtceu:steel_mining_hammer");
        SlotWorkspaceViewModel.KitCard activeKit = result.viewModel().activeKit();

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertTrue(hammerCard.carried());
        assertFalse(hammerCard.ghost());
        assertEquals(1, hammerCard.totalCount());
        assertFalse(hammerCard.kitNeeded());
        assertEquals("kit-1", activeKit.kitId());
        assertEquals(1, activeKit.readyCount());
        assertTrue(result.viewModel().wayfindingTargets().isEmpty());
        assertTrue(result.viewModel().contextualSuggestionLanes().isEmpty());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleWorkflowCarriedRemovalInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity hammer = new ItemIdentity("gtceu:steel_mining_hammer", ItemComparisonMode.ITEM_ID, "");
        KitDefinition kit = new KitDefinition(
                "kit-1",
                "Mining",
                List.of(KitPage.empty().withSlot(0, hammer)),
                null);
        WorkflowDomainSnapshot workflow = workflowWithKitMap(
                VisualHomeMap.empty(),
                new KitMap(List.of(kit), new KitActivation(kit.id(), 0, Set.of())));
        WorkspaceProjectionRequest seed = request(
                authority("gtceu:steel_mining_hammer", 1),
                workflow,
                "ready",
                "",
                0,
                0,
                "");
        WorkspaceProjectionRequest changed = request(
                InventoryAuthoritySnapshot.empty(),
                workflow,
                "ready",
                "",
                0,
                0,
                "");
        WorkspaceInvalidation invalidation = WorkspaceInvalidation.localizedIdentity(
                WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED,
                hammer,
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.WORKFLOW,
                        WorkspaceProjectionSlice.CONTEXTUAL,
                        WorkspaceProjectionSlice.WAYFINDING),
                "simple_workflow_carried_remove");
        SlotWorkspaceViewModel.setGhostStackResolver(itemId -> new ItemStack(itemId, 1, 64));

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.AtlasItem hammerGhost = card(result.viewModel(), "gtceu:steel_mining_hammer");
        SlotWorkspaceViewModel.KitCard activeKit = result.viewModel().activeKit();
        SlotWorkspaceViewModel.ContextualSuggestionLane fetchLane = result.viewModel().contextualSuggestionLanes()
                .stream()
                .filter(SlotWorkspaceViewModel.ContextualSuggestionLane::fetch)
                .findFirst()
                .orElseThrow();

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertTrue(hammerGhost.ghost());
        assertTrue(hammerGhost.kitNeeded());
        assertEquals("kit-1", activeKit.kitId());
        assertEquals(0, activeKit.readyCount());
        assertTrue(result.viewModel().wayfindingTargets().isEmpty());
        assertEquals(1, fetchLane.items().size());
        assertEquals("gtceu:steel_mining_hammer", fetchLane.items().get(0).identity().itemId());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleWorkflowCarriedAcquireWithClaimedStorageWayfindingSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity hammer = new ItemIdentity("gtceu:steel_mining_hammer", ItemComparisonMode.ITEM_ID, "");
        UUID storageId = UUID.fromString("00000000-0000-0000-0000-000000000099");
        ClaimedChest chest = claimedChest(storageId);
        ClaimedChestMap claimedChests = new ClaimedChestMap(List.of(chest));
        KitDefinition kit = new KitDefinition(
                "kit-1",
                "Mining",
                List.of(KitPage.empty().withSlot(0, hammer)),
                null);
        WorkflowDomainSnapshot workflow = workflowWithKitMapAndChests(
                VisualHomeMap.empty(),
                new KitMap(List.of(kit), new KitActivation(kit.id(), 0, Set.of())),
                claimedChests);
        WorkspaceStorageIndex index = storageIndex(
                List.of(claimedStorageEntry(storageId, "gtceu:steel_mining_hammer", 1, true)),
                1L);
        WorkspaceProjectionRequest seed = storageRequestWithResolverAndProximateIds(
                InventoryAuthoritySnapshot.empty(),
                workflow,
                index,
                Set.of(storageId.toString()));
        WorkspaceProjectionRequest changed = storageRequestWithResolverAndProximateIds(
                authority("gtceu:steel_mining_hammer", 1),
                workflow,
                index,
                Set.of(storageId.toString()));
        WorkspaceInvalidation invalidation = WorkspaceInvalidation.localizedIdentity(
                WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED,
                hammer,
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.WORKFLOW,
                        WorkspaceProjectionSlice.CONTEXTUAL,
                        WorkspaceProjectionSlice.WAYFINDING),
                "simple_workflow_carried_acquire_claimed_storage");
        SlotWorkspaceViewModel.setGhostStackResolver(itemId -> new ItemStack(itemId, 1, 64));

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.AtlasItem hammerCard = card(result.viewModel(), "gtceu:steel_mining_hammer");
        SlotWorkspaceViewModel.KitCard activeKit = result.viewModel().activeKit();

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertTrue(hammerCard.carried());
        assertFalse(hammerCard.ghost());
        assertEquals(1, hammerCard.proximateCount());
        assertFalse(hammerCard.kitNeeded());
        assertEquals("kit-1", activeKit.kitId());
        assertEquals(1, activeKit.readyCount());
        assertTrue(result.viewModel().wayfindingTargets().isEmpty());
        assertTrue(result.viewModel().contextualSuggestionLanes().isEmpty());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleWorkflowCarriedRemovalWithClaimedStorageWayfindingSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity hammer = new ItemIdentity("gtceu:steel_mining_hammer", ItemComparisonMode.ITEM_ID, "");
        UUID storageId = UUID.fromString("00000000-0000-0000-0000-000000000100");
        ClaimedChest chest = claimedChest(storageId);
        ClaimedChestMap claimedChests = new ClaimedChestMap(List.of(chest));
        KitDefinition kit = new KitDefinition(
                "kit-1",
                "Mining",
                List.of(KitPage.empty().withSlot(0, hammer)),
                null);
        WorkflowDomainSnapshot workflow = workflowWithKitMapAndChests(
                VisualHomeMap.empty(),
                new KitMap(List.of(kit), new KitActivation(kit.id(), 0, Set.of())),
                claimedChests);
        WorkspaceStorageIndex index = storageIndex(
                List.of(claimedStorageEntry(storageId, "gtceu:steel_mining_hammer", 1, true)),
                1L);
        WorkspaceProjectionRequest seed = storageRequestWithResolverAndProximateIds(
                authority("gtceu:steel_mining_hammer", 1),
                workflow,
                index,
                Set.of(storageId.toString()));
        WorkspaceProjectionRequest changed = storageRequestWithResolverAndProximateIds(
                InventoryAuthoritySnapshot.empty(),
                workflow,
                index,
                Set.of(storageId.toString()));
        WorkspaceInvalidation invalidation = WorkspaceInvalidation.localizedIdentity(
                WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED,
                hammer,
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.WORKFLOW,
                        WorkspaceProjectionSlice.CONTEXTUAL,
                        WorkspaceProjectionSlice.WAYFINDING),
                "simple_workflow_carried_remove_claimed_storage");
        SlotWorkspaceViewModel.setGhostStackResolver(itemId -> new ItemStack(itemId, 1, 64));

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.AtlasItem hammerGhost = card(result.viewModel(), "gtceu:steel_mining_hammer");
        SlotWorkspaceViewModel.KitCard activeKit = result.viewModel().activeKit();
        SlotWorkspaceViewModel.ContextualSuggestionLane fetchLane = result.viewModel().contextualSuggestionLanes()
                .stream()
                .filter(SlotWorkspaceViewModel.ContextualSuggestionLane::fetch)
                .findFirst()
                .orElseThrow();
        WayfindingTarget target = result.viewModel().wayfindingTargets().get(0);

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertFalse(hammerGhost.carried());
        assertTrue(hammerGhost.ghost());
        assertTrue(hammerGhost.kitNeeded());
        assertEquals(1, hammerGhost.proximateCount());
        assertEquals("kit-1", activeKit.kitId());
        assertEquals(0, activeKit.readyCount());
        assertEquals(1, fetchLane.items().size());
        assertEquals("gtceu:steel_mining_hammer", fetchLane.items().get(0).identity().itemId());
        assertEquals(1, result.viewModel().wayfindingTargets().size());
        assertEquals(storageId.toString(), target.storageId());
        assertEquals(WayfindingTarget.Scope.KIT, target.scope());
        assertTrue(target.kitMissingIdentities().contains(hammer));
        assertEquals(1, target.totalMissingCount());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleWorkflowScopedDesiredActivationInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity rope = new ItemIdentity("farmersdelight:rope", ItemComparisonMode.ITEM_ID, "");
        KitDefinition kit = new KitDefinition(
                "kit-1",
                "Mining",
                List.of(KitPage.empty()),
                null);
        WorkflowDomainSnapshot seedWorkflow = workflowWithKitMapCountsAndChests(
                VisualHomeMap.empty(),
                new KitMap(List.of(kit), KitActivation.NONE),
                Map.of(kit.id(), Map.of(rope, 12)),
                Map.of(),
                null);
        WorkflowDomainSnapshot changedWorkflow = workflowWithKitMapCountsAndChests(
                VisualHomeMap.empty(),
                new KitMap(List.of(kit), new KitActivation(kit.id(), 0)),
                Map.of(kit.id(), Map.of(rope, 12)),
                Map.of(),
                null);
        WorkspaceProjectionRequest seed = storageRequest(
                InventoryAuthoritySnapshot.empty(),
                seedWorkflow,
                WorkspaceStorageIndex.empty());
        WorkspaceProjectionRequest changed = storageRequest(
                InventoryAuthoritySnapshot.empty(),
                changedWorkflow,
                WorkspaceStorageIndex.empty());
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.WORKFLOW_SEQUENCE_CHANGED,
                Set.of(rope),
                Set.of(),
                Set.of(),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.WORKFLOW,
                        WorkspaceProjectionSlice.CONTEXTUAL,
                        WorkspaceProjectionSlice.WAYFINDING,
                        WorkspaceProjectionSlice.FRAME),
                false,
                "simple_workflow_scoped_desired_activate");
        SlotWorkspaceViewModel.setGhostStackResolver(itemId -> new ItemStack(itemId, 1, 64));

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.AtlasItem ghost = card(result.viewModel(), "farmersdelight:rope");

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertTrue(ghost.ghost());
        assertTrue(ghost.kitNeeded());
        assertEquals(12, ghost.desiredCount());
        assertTrue(ghost.desiredCountFromKit());
        assertEquals(0, ghost.wantedCount());
        assertTrue(result.viewModel().wayfindingTargets().isEmpty());
        assertEquals("kit-1", result.viewModel().activeKit().kitId());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleWorkflowMemberActivationInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity torch = new ItemIdentity("minecraft:torch", ItemComparisonMode.ITEM_ID, "");
        KitDefinition kit = new KitDefinition(
                "kit-1",
                "Mining",
                List.of(KitPage.empty()),
                null,
                "",
                Set.of(torch));
        WorkflowDomainSnapshot seedWorkflow = workflowWithKitMap(
                VisualHomeMap.empty(),
                new KitMap(List.of(kit), KitActivation.NONE));
        WorkflowDomainSnapshot changedWorkflow = workflowWithKitMap(
                VisualHomeMap.empty(),
                new KitMap(List.of(kit), new KitActivation(kit.id(), 0)));
        WorkspaceProjectionRequest seed = storageRequest(
                InventoryAuthoritySnapshot.empty(),
                seedWorkflow,
                WorkspaceStorageIndex.empty());
        WorkspaceProjectionRequest changed = storageRequest(
                InventoryAuthoritySnapshot.empty(),
                changedWorkflow,
                WorkspaceStorageIndex.empty());
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.WORKFLOW_SEQUENCE_CHANGED,
                Set.of(torch),
                Set.of(),
                Set.of(),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.WORKFLOW,
                        WorkspaceProjectionSlice.CONTEXTUAL,
                        WorkspaceProjectionSlice.WAYFINDING,
                        WorkspaceProjectionSlice.FRAME),
                false,
                "simple_workflow_member_activate");
        SlotWorkspaceViewModel.setGhostStackResolver(itemId -> new ItemStack(itemId, 1, 64));

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.AtlasItem ghost = card(result.viewModel(), "minecraft:torch");

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertTrue(ghost.ghost());
        assertTrue(ghost.kitNeeded());
        assertEquals(0, ghost.desiredCount());
        assertFalse(ghost.desiredCountFromKit());
        assertEquals(1, ghost.wantedCount());
        assertTrue(result.viewModel().wayfindingTargets().isEmpty());
        assertEquals("kit-1", result.viewModel().activeKit().kitId());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleWorkflowPutAwayActivationInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity hammer = new ItemIdentity("gtceu:steel_mining_hammer", ItemComparisonMode.ITEM_ID, "");
        ItemIdentity dirt = new ItemIdentity("minecraft:dirt", ItemComparisonMode.ITEM_ID, "");
        KitDefinition kit = new KitDefinition(
                "kit-1",
                "Mining",
                List.of(KitPage.empty().withSlot(0, hammer)),
                null);
        InventoryAuthoritySnapshot authority = authority(List.of(stack(4, "minecraft:dirt", 32)));
        WorkflowDomainSnapshot seedWorkflow = workflowWithKitMap(
                VisualHomeMap.empty(),
                new KitMap(List.of(kit), KitActivation.NONE));
        WorkflowDomainSnapshot changedWorkflow = workflowWithKitMap(
                VisualHomeMap.empty(),
                new KitMap(List.of(kit), new KitActivation(kit.id(), 0, Set.of(dirt))));
        WorkspaceProjectionRequest seed = storageRequest(
                authority,
                seedWorkflow,
                WorkspaceStorageIndex.empty());
        WorkspaceProjectionRequest changed = storageRequest(
                authority,
                changedWorkflow,
                WorkspaceStorageIndex.empty());
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.WORKFLOW_SEQUENCE_CHANGED,
                Set.of(hammer, dirt),
                Set.of(),
                Set.of(),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.WORKFLOW,
                        WorkspaceProjectionSlice.CONTEXTUAL,
                        WorkspaceProjectionSlice.WAYFINDING,
                        WorkspaceProjectionSlice.FRAME),
                false,
                "simple_workflow_put_away_activate");
        SlotWorkspaceViewModel.setGhostStackResolver(itemId -> new ItemStack(itemId, 1, 64));

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.AtlasItem hammerGhost = card(result.viewModel(), "gtceu:steel_mining_hammer");
        SlotWorkspaceViewModel.AtlasItem dirtCard = card(result.viewModel(), "minecraft:dirt");
        SlotWorkspaceViewModel.ContextualSuggestionLane putAwayLane = result.viewModel().contextualSuggestionLanes()
                .stream()
                .filter(SlotWorkspaceViewModel.ContextualSuggestionLane::putAway)
                .findFirst()
                .orElseThrow();

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertTrue(hammerGhost.ghost());
        assertTrue(hammerGhost.kitNeeded());
        assertTrue(dirtCard.carried());
        assertFalse(dirtCard.ghost());
        assertEquals(32, dirtCard.totalCount());
        assertEquals(SlotWorkspaceViewModel.PutAwayState.NO_ROUTE, dirtCard.putAwayState());
        assertEquals(1, putAwayLane.items().size());
        assertEquals("minecraft:dirt", putAwayLane.items().get(0).identity().itemId());
        assertEquals(SlotWorkspaceViewModel.PutAwayState.NO_ROUTE, putAwayLane.items().get(0).putAwayState());
        assertEquals("kit-1", result.viewModel().activeKit().kitId());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleWorkflowPutAwayDeactivationInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity hammer = new ItemIdentity("gtceu:steel_mining_hammer", ItemComparisonMode.ITEM_ID, "");
        ItemIdentity dirt = new ItemIdentity("minecraft:dirt", ItemComparisonMode.ITEM_ID, "");
        KitDefinition kit = new KitDefinition(
                "kit-1",
                "Mining",
                List.of(KitPage.empty().withSlot(0, hammer)),
                null);
        InventoryAuthoritySnapshot authority = authority(List.of(stack(4, "minecraft:dirt", 32)));
        WorkflowDomainSnapshot seedWorkflow = workflowWithKitMap(
                VisualHomeMap.empty(),
                new KitMap(List.of(kit), new KitActivation(kit.id(), 0, Set.of(dirt))));
        WorkflowDomainSnapshot changedWorkflow = workflowWithKitMap(
                VisualHomeMap.empty(),
                new KitMap(List.of(kit), KitActivation.NONE));
        WorkspaceProjectionRequest seed = storageRequest(
                authority,
                seedWorkflow,
                WorkspaceStorageIndex.empty());
        WorkspaceProjectionRequest changed = storageRequest(
                authority,
                changedWorkflow,
                WorkspaceStorageIndex.empty());
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.WORKFLOW_SEQUENCE_CHANGED,
                Set.of(hammer, dirt),
                Set.of(),
                Set.of(),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.WORKFLOW,
                        WorkspaceProjectionSlice.CONTEXTUAL,
                        WorkspaceProjectionSlice.WAYFINDING,
                        WorkspaceProjectionSlice.FRAME),
                false,
                "simple_workflow_put_away_deactivate");
        SlotWorkspaceViewModel.setGhostStackResolver(itemId -> new ItemStack(itemId, 1, 64));

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.AtlasItem dirtCard = card(result.viewModel(), "minecraft:dirt");

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertFalse(hasCard(result.viewModel(), "gtceu:steel_mining_hammer"));
        assertTrue(dirtCard.carried());
        assertFalse(dirtCard.ghost());
        assertEquals(32, dirtCard.totalCount());
        assertEquals(SlotWorkspaceViewModel.PutAwayState.NONE, dirtCard.putAwayState());
        assertTrue(result.viewModel().contextualSuggestionLanes().stream().noneMatch(
                SlotWorkspaceViewModel.ContextualSuggestionLane::putAway));
        assertNull(result.viewModel().activeKit());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleWorkflowRoutedPutAwayActivationInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity hammer = new ItemIdentity("gtceu:steel_mining_hammer", ItemComparisonMode.ITEM_ID, "");
        ItemIdentity dirt = new ItemIdentity("minecraft:dirt", ItemComparisonMode.ITEM_ID, "");
        UUID storageId = UUID.fromString("00000000-0000-0000-0000-000000000089");
        ClaimedChest chest = claimedChest(storageId);
        ClaimedChestMap claimedChests = new ClaimedChestMap(List.of(chest));
        KitDefinition kit = new KitDefinition(
                "kit-1",
                "Mining",
                List.of(KitPage.empty().withSlot(0, hammer)),
                null);
        InventoryAuthoritySnapshot authority = authority(List.of(stack(4, "minecraft:dirt", 32)));
        WorkflowDomainSnapshot seedWorkflow = workflowWithKitMapAndChests(
                VisualHomeMap.empty(),
                new KitMap(List.of(kit), KitActivation.NONE),
                claimedChests);
        WorkflowDomainSnapshot changedWorkflow = workflowWithKitMapAndChests(
                VisualHomeMap.empty(),
                new KitMap(List.of(kit), new KitActivation(kit.id(), 0, Set.of(dirt))),
                claimedChests);
        WorkspaceStorageIndex index = storageIndex(
                List.of(claimedStorageEntry(storageId, "minecraft:dirt", 64, true)),
                1L);
        WorkspaceProjectionRequest seed = storageRequestWithResolverAndProximateIds(
                authority,
                seedWorkflow,
                index,
                Set.of(storageId.toString()));
        WorkspaceProjectionRequest changed = storageRequestWithResolverAndProximateIds(
                authority,
                changedWorkflow,
                index,
                Set.of(storageId.toString()));
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.WORKFLOW_SEQUENCE_CHANGED,
                Set.of(hammer, dirt),
                Set.of(),
                Set.of(),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.WORKFLOW,
                        WorkspaceProjectionSlice.CONTEXTUAL,
                        WorkspaceProjectionSlice.WAYFINDING,
                        WorkspaceProjectionSlice.FRAME),
                false,
                "simple_workflow_routed_put_away_activate");
        SlotWorkspaceViewModel.setGhostStackResolver(itemId -> new ItemStack(itemId, 1, 64));

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.AtlasItem dirtCard = card(result.viewModel(), "minecraft:dirt");
        SlotWorkspaceViewModel.ContextualSuggestionLane putAwayLane = result.viewModel().contextualSuggestionLanes()
                .stream()
                .filter(SlotWorkspaceViewModel.ContextualSuggestionLane::putAway)
                .findFirst()
                .orElseThrow();
        WayfindingTarget target = result.viewModel().wayfindingTargets().get(0);

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertTrue(dirtCard.carried());
        assertFalse(dirtCard.ghost());
        assertEquals(32, dirtCard.totalCount());
        assertEquals(SlotWorkspaceViewModel.PutAwayState.ROUTED, dirtCard.putAwayState());
        assertEquals(1, putAwayLane.items().size());
        assertEquals("minecraft:dirt", putAwayLane.items().get(0).identity().itemId());
        assertEquals(SlotWorkspaceViewModel.PutAwayState.ROUTED, putAwayLane.items().get(0).putAwayState());
        assertEquals(1, result.viewModel().wayfindingTargets().size());
        assertEquals(storageId.toString(), target.storageId());
        assertEquals(WayfindingTarget.Scope.PUT_AWAY, target.scope());
        assertTrue(target.putAwayIdentities().contains(dirt));
        assertEquals(32, target.totalMissingCount());
        assertEquals("kit-1", result.viewModel().activeKit().kitId());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleWorkflowLiveHookPutAwayActivationInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity hammer = new ItemIdentity("gtceu:steel_mining_hammer", ItemComparisonMode.ITEM_ID, "");
        ItemIdentity dirt = new ItemIdentity("minecraft:dirt", ItemComparisonMode.ITEM_ID, "");
        UUID storageId = UUID.fromString("00000000-0000-0000-0000-000000000096");
        ClaimedChest chest = claimedChest(storageId);
        ClaimedChestMap claimedChests = new ClaimedChestMap(List.of(chest));
        KitDefinition kit = new KitDefinition(
                "kit-1",
                "Mining",
                List.of(KitPage.empty().withSlot(0, hammer)),
                null);
        InventoryAuthoritySnapshot authority = authority(List.of(stack(4, "minecraft:dirt", 32)));
        WorkflowDomainSnapshot seedWorkflow = workflowWithKitMapAndChests(
                VisualHomeMap.empty(),
                new KitMap(List.of(kit), KitActivation.NONE),
                claimedChests);
        WorkflowDomainSnapshot changedWorkflow = workflowWithKitMapAndChests(
                VisualHomeMap.empty(),
                new KitMap(List.of(kit), new KitActivation(kit.id(), 0, Set.of(dirt))),
                claimedChests);
        WorkspaceStorageIndex index = storageIndex(
                List.of(claimedEmptyStorageEntry(storageId, true)),
                1L);
        DepositPlanner.ChestContentPresence livePresence = (candidateChest, identity) ->
                candidateChest != null
                        && storageId.equals(candidateChest.storageId())
                        && identity != null
                        && "minecraft:dirt".equals(identity.itemId());
        DepositPlanner.ChestEligibility liveEligibility = candidateChest ->
                candidateChest != null && storageId.equals(candidateChest.storageId());
        WorkspaceProjectionRequest seed = storageRequestWithResolverProximateDepositAndLiveHooks(
                authority,
                seedWorkflow,
                index,
                Set.of(storageId.toString()),
                Set.of(),
                livePresence,
                liveEligibility);
        WorkspaceProjectionRequest changed = storageRequestWithResolverProximateDepositAndLiveHooks(
                authority,
                changedWorkflow,
                index,
                Set.of(storageId.toString()),
                Set.of(),
                livePresence,
                liveEligibility);
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.WORKFLOW_SEQUENCE_CHANGED,
                Set.of(hammer, dirt),
                Set.of(),
                Set.of(),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.WORKFLOW,
                        WorkspaceProjectionSlice.CONTEXTUAL,
                        WorkspaceProjectionSlice.WAYFINDING,
                        WorkspaceProjectionSlice.FRAME),
                false,
                "simple_workflow_live_hook_put_away_activate");
        SlotWorkspaceViewModel.setGhostStackResolver(itemId -> new ItemStack(itemId, 1, 64));

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.AtlasItem dirtCard = card(result.viewModel(), "minecraft:dirt");
        SlotWorkspaceViewModel.ContextualSuggestionLane putAwayLane = result.viewModel().contextualSuggestionLanes()
                .stream()
                .filter(SlotWorkspaceViewModel.ContextualSuggestionLane::putAway)
                .findFirst()
                .orElseThrow();
        WayfindingTarget target = result.viewModel().wayfindingTargets().get(0);

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertTrue(dirtCard.carried());
        assertFalse(dirtCard.ghost());
        assertEquals(32, dirtCard.totalCount());
        assertEquals(SlotWorkspaceViewModel.PutAwayState.ROUTED, dirtCard.putAwayState());
        assertEquals(1, putAwayLane.items().size());
        assertEquals("minecraft:dirt", putAwayLane.items().get(0).identity().itemId());
        assertEquals(SlotWorkspaceViewModel.PutAwayState.ROUTED, putAwayLane.items().get(0).putAwayState());
        assertEquals(1, result.viewModel().wayfindingTargets().size());
        assertEquals(storageId.toString(), target.storageId());
        assertEquals(WayfindingTarget.Scope.PUT_AWAY, target.scope());
        assertTrue(target.putAwayIdentities().contains(dirt));
        assertEquals(32, target.totalMissingCount());
        assertEquals("kit-1", result.viewModel().activeKit().kitId());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleWorkflowDepositEligiblePutAwayActivationInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity hammer = new ItemIdentity("gtceu:steel_mining_hammer", ItemComparisonMode.ITEM_ID, "");
        ItemIdentity dirt = new ItemIdentity("minecraft:dirt", ItemComparisonMode.ITEM_ID, "");
        UUID storageId = UUID.fromString("00000000-0000-0000-0000-000000000098");
        ClaimedChest chest = claimedChest(storageId);
        ClaimedChestMap claimedChests = new ClaimedChestMap(List.of(chest));
        KitDefinition kit = new KitDefinition(
                "kit-1",
                "Mining",
                List.of(KitPage.empty().withSlot(0, hammer)),
                null);
        InventoryAuthoritySnapshot authority = authority(List.of(stack(4, "minecraft:dirt", 32)));
        ChestAffinityMap affinity = affinity(storageId, dirt, 1);
        WorkflowDomainSnapshot seedWorkflow = workflowWithKitMapChestsAndAffinity(
                VisualHomeMap.empty(),
                new KitMap(List.of(kit), KitActivation.NONE),
                claimedChests,
                affinity);
        WorkflowDomainSnapshot changedWorkflow = workflowWithKitMapChestsAndAffinity(
                VisualHomeMap.empty(),
                new KitMap(List.of(kit), new KitActivation(kit.id(), 0, Set.of(dirt))),
                claimedChests,
                affinity);
        WorkspaceStorageIndex index = storageIndex(
                List.of(claimedEmptyStorageEntry(storageId, true)),
                1L);
        WorkspaceProjectionRequest seed = storageRequestWithResolverProximateAndDepositIds(
                authority,
                seedWorkflow,
                index,
                Set.of(storageId.toString()),
                Set.of(storageId.toString()));
        WorkspaceProjectionRequest changed = storageRequestWithResolverProximateAndDepositIds(
                authority,
                changedWorkflow,
                index,
                Set.of(storageId.toString()),
                Set.of(storageId.toString()));
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.WORKFLOW_SEQUENCE_CHANGED,
                Set.of(hammer, dirt),
                Set.of(),
                Set.of(),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.WORKFLOW,
                        WorkspaceProjectionSlice.CONTEXTUAL,
                        WorkspaceProjectionSlice.WAYFINDING,
                        WorkspaceProjectionSlice.FRAME),
                false,
                "simple_workflow_deposit_eligible_put_away_activate");
        SlotWorkspaceViewModel.setGhostStackResolver(itemId -> new ItemStack(itemId, 1, 64));

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.AtlasItem dirtCard = card(result.viewModel(), "minecraft:dirt");
        SlotWorkspaceViewModel.ContextualSuggestionLane putAwayLane = result.viewModel().contextualSuggestionLanes()
                .stream()
                .filter(SlotWorkspaceViewModel.ContextualSuggestionLane::putAway)
                .findFirst()
                .orElseThrow();
        WayfindingTarget target = result.viewModel().wayfindingTargets().get(0);

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertTrue(dirtCard.carried());
        assertFalse(dirtCard.ghost());
        assertEquals(32, dirtCard.totalCount());
        assertEquals(SlotWorkspaceViewModel.PutAwayState.ROUTED, dirtCard.putAwayState());
        assertEquals(1, putAwayLane.items().size());
        assertEquals("minecraft:dirt", putAwayLane.items().get(0).identity().itemId());
        assertEquals(SlotWorkspaceViewModel.PutAwayState.ROUTED, putAwayLane.items().get(0).putAwayState());
        assertEquals(1, result.viewModel().wayfindingTargets().size());
        assertEquals(storageId.toString(), target.storageId());
        assertEquals(WayfindingTarget.Scope.PUT_AWAY, target.scope());
        assertTrue(target.putAwayIdentities().contains(dirt));
        assertEquals(32, target.totalMissingCount());
        assertTrue(result.viewModel().depositableIdentities().isEmpty());
        assertEquals("kit-1", result.viewModel().activeKit().kitId());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleWorkflowLiveHookPutAwayDeactivationInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity hammer = new ItemIdentity("gtceu:steel_mining_hammer", ItemComparisonMode.ITEM_ID, "");
        ItemIdentity dirt = new ItemIdentity("minecraft:dirt", ItemComparisonMode.ITEM_ID, "");
        UUID storageId = UUID.fromString("00000000-0000-0000-0000-000000000097");
        ClaimedChest chest = claimedChest(storageId);
        ClaimedChestMap claimedChests = new ClaimedChestMap(List.of(chest));
        KitDefinition kit = new KitDefinition(
                "kit-1",
                "Mining",
                List.of(KitPage.empty().withSlot(0, hammer)),
                null);
        InventoryAuthoritySnapshot authority = authority(List.of(stack(4, "minecraft:dirt", 32)));
        WorkflowDomainSnapshot seedWorkflow = workflowWithKitMapAndChests(
                VisualHomeMap.empty(),
                new KitMap(List.of(kit), new KitActivation(kit.id(), 0, Set.of(dirt))),
                claimedChests);
        WorkflowDomainSnapshot changedWorkflow = workflowWithKitMapAndChests(
                VisualHomeMap.empty(),
                new KitMap(List.of(kit), KitActivation.NONE),
                claimedChests);
        WorkspaceStorageIndex index = storageIndex(
                List.of(claimedEmptyStorageEntry(storageId, true)),
                1L);
        DepositPlanner.ChestContentPresence livePresence = (candidateChest, identity) ->
                candidateChest != null
                        && storageId.equals(candidateChest.storageId())
                        && identity != null
                        && "minecraft:dirt".equals(identity.itemId());
        DepositPlanner.ChestEligibility liveEligibility = candidateChest ->
                candidateChest != null && storageId.equals(candidateChest.storageId());
        WorkspaceProjectionRequest seed = storageRequestWithResolverProximateDepositAndLiveHooks(
                authority,
                seedWorkflow,
                index,
                Set.of(storageId.toString()),
                Set.of(),
                livePresence,
                liveEligibility);
        WorkspaceProjectionRequest changed = storageRequestWithResolverProximateDepositAndLiveHooks(
                authority,
                changedWorkflow,
                index,
                Set.of(storageId.toString()),
                Set.of(),
                livePresence,
                liveEligibility);
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.WORKFLOW_SEQUENCE_CHANGED,
                Set.of(hammer, dirt),
                Set.of(),
                Set.of(),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.WORKFLOW,
                        WorkspaceProjectionSlice.CONTEXTUAL,
                        WorkspaceProjectionSlice.WAYFINDING,
                        WorkspaceProjectionSlice.FRAME),
                false,
                "simple_workflow_live_hook_put_away_deactivate");
        SlotWorkspaceViewModel.setGhostStackResolver(itemId -> new ItemStack(itemId, 1, 64));

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.AtlasItem dirtCard = card(result.viewModel(), "minecraft:dirt");

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertFalse(hasCard(result.viewModel(), "gtceu:steel_mining_hammer"));
        assertTrue(dirtCard.carried());
        assertFalse(dirtCard.ghost());
        assertEquals(32, dirtCard.totalCount());
        assertEquals(SlotWorkspaceViewModel.PutAwayState.NONE, dirtCard.putAwayState());
        assertTrue(result.viewModel().contextualSuggestionLanes().stream().noneMatch(
                SlotWorkspaceViewModel.ContextualSuggestionLane::putAway));
        assertTrue(result.viewModel().wayfindingTargets().isEmpty());
        assertNull(result.viewModel().activeKit());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleWorkflowRoutedPutAwayDeactivationInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity hammer = new ItemIdentity("gtceu:steel_mining_hammer", ItemComparisonMode.ITEM_ID, "");
        ItemIdentity dirt = new ItemIdentity("minecraft:dirt", ItemComparisonMode.ITEM_ID, "");
        UUID storageId = UUID.fromString("00000000-0000-0000-0000-000000000090");
        ClaimedChest chest = claimedChest(storageId);
        ClaimedChestMap claimedChests = new ClaimedChestMap(List.of(chest));
        KitDefinition kit = new KitDefinition(
                "kit-1",
                "Mining",
                List.of(KitPage.empty().withSlot(0, hammer)),
                null);
        InventoryAuthoritySnapshot authority = authority(List.of(stack(4, "minecraft:dirt", 32)));
        WorkflowDomainSnapshot seedWorkflow = workflowWithKitMapAndChests(
                VisualHomeMap.empty(),
                new KitMap(List.of(kit), new KitActivation(kit.id(), 0, Set.of(dirt))),
                claimedChests);
        WorkflowDomainSnapshot changedWorkflow = workflowWithKitMapAndChests(
                VisualHomeMap.empty(),
                new KitMap(List.of(kit), KitActivation.NONE),
                claimedChests);
        WorkspaceStorageIndex index = storageIndex(
                List.of(claimedStorageEntry(storageId, "minecraft:dirt", 64, true)),
                1L);
        WorkspaceProjectionRequest seed = storageRequestWithResolverAndProximateIds(
                authority,
                seedWorkflow,
                index,
                Set.of(storageId.toString()));
        WorkspaceProjectionRequest changed = storageRequestWithResolverAndProximateIds(
                authority,
                changedWorkflow,
                index,
                Set.of(storageId.toString()));
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.WORKFLOW_SEQUENCE_CHANGED,
                Set.of(hammer, dirt),
                Set.of(),
                Set.of(),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.WORKFLOW,
                        WorkspaceProjectionSlice.CONTEXTUAL,
                        WorkspaceProjectionSlice.WAYFINDING,
                        WorkspaceProjectionSlice.FRAME),
                false,
                "simple_workflow_routed_put_away_deactivate");
        SlotWorkspaceViewModel.setGhostStackResolver(itemId -> new ItemStack(itemId, 1, 64));

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.AtlasItem dirtCard = card(result.viewModel(), "minecraft:dirt");

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertFalse(hasCard(result.viewModel(), "gtceu:steel_mining_hammer"));
        assertTrue(dirtCard.carried());
        assertFalse(dirtCard.ghost());
        assertEquals(32, dirtCard.totalCount());
        assertEquals(SlotWorkspaceViewModel.PutAwayState.NONE, dirtCard.putAwayState());
        assertTrue(result.viewModel().contextualSuggestionLanes().stream().noneMatch(
                SlotWorkspaceViewModel.ContextualSuggestionLane::putAway));
        assertTrue(result.viewModel().wayfindingTargets().isEmpty());
        assertNull(result.viewModel().activeKit());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleWorkflowStoragePutAwayRouteAppearsSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity hammer = new ItemIdentity("gtceu:steel_mining_hammer", ItemComparisonMode.ITEM_ID, "");
        ItemIdentity dirt = new ItemIdentity("minecraft:dirt", ItemComparisonMode.ITEM_ID, "");
        UUID storageId = UUID.fromString("00000000-0000-0000-0000-000000000091");
        ClaimedChest chest = claimedChest(storageId);
        ClaimedChestMap claimedChests = new ClaimedChestMap(List.of(chest));
        KitDefinition kit = new KitDefinition(
                "kit-1",
                "Mining",
                List.of(KitPage.empty().withSlot(0, hammer)),
                null);
        InventoryAuthoritySnapshot authority = authority(List.of(stack(4, "minecraft:dirt", 32)));
        WorkflowDomainSnapshot workflow = workflowWithKitMapAndChests(
                VisualHomeMap.empty(),
                new KitMap(List.of(kit), new KitActivation(kit.id(), 0, Set.of(dirt))),
                claimedChests);
        WorkspaceProjectionRequest seed = storageRequestWithResolverAndProximateIds(
                authority,
                workflow,
                storageIndex(List.of(claimedEmptyStorageEntry(storageId, true)), 1L),
                Set.of(storageId.toString()));
        WorkspaceProjectionRequest changed = storageRequestWithResolverAndProximateIds(
                authority,
                workflow,
                storageIndex(List.of(claimedStorageEntry(storageId, "minecraft:dirt", 64, true)), 2L),
                Set.of(storageId.toString()));
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED,
                Set.of(),
                Set.of(storageId.toString()),
                Set.of(),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.STORAGE,
                        WorkspaceProjectionSlice.WORKFLOW,
                        WorkspaceProjectionSlice.CONTEXTUAL,
                        WorkspaceProjectionSlice.WAYFINDING,
                        WorkspaceProjectionSlice.FRAME),
                false,
                "simple_workflow_storage_put_away_route_add");
        SlotWorkspaceViewModel.setGhostStackResolver(itemId -> new ItemStack(itemId, 1, 64));

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.AtlasItem dirtCard = card(result.viewModel(), "minecraft:dirt");
        SlotWorkspaceViewModel.ContextualSuggestionLane putAwayLane = result.viewModel().contextualSuggestionLanes()
                .stream()
                .filter(SlotWorkspaceViewModel.ContextualSuggestionLane::putAway)
                .findFirst()
                .orElseThrow();
        WayfindingTarget target = result.viewModel().wayfindingTargets().get(0);

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertTrue(dirtCard.carried());
        assertEquals(SlotWorkspaceViewModel.PutAwayState.ROUTED, dirtCard.putAwayState());
        assertEquals(1, putAwayLane.items().size());
        assertEquals(SlotWorkspaceViewModel.PutAwayState.ROUTED, putAwayLane.items().get(0).putAwayState());
        assertEquals(1, result.viewModel().wayfindingTargets().size());
        assertEquals(storageId.toString(), target.storageId());
        assertEquals(WayfindingTarget.Scope.PUT_AWAY, target.scope());
        assertTrue(target.putAwayIdentities().contains(dirt));
        assertEquals("kit-1", result.viewModel().activeKit().kitId());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleWorkflowStoragePutAwayRouteDisappearsSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity hammer = new ItemIdentity("gtceu:steel_mining_hammer", ItemComparisonMode.ITEM_ID, "");
        ItemIdentity dirt = new ItemIdentity("minecraft:dirt", ItemComparisonMode.ITEM_ID, "");
        UUID storageId = UUID.fromString("00000000-0000-0000-0000-000000000092");
        ClaimedChest chest = claimedChest(storageId);
        ClaimedChestMap claimedChests = new ClaimedChestMap(List.of(chest));
        KitDefinition kit = new KitDefinition(
                "kit-1",
                "Mining",
                List.of(KitPage.empty().withSlot(0, hammer)),
                null);
        InventoryAuthoritySnapshot authority = authority(List.of(stack(4, "minecraft:dirt", 32)));
        WorkflowDomainSnapshot workflow = workflowWithKitMapAndChests(
                VisualHomeMap.empty(),
                new KitMap(List.of(kit), new KitActivation(kit.id(), 0, Set.of(dirt))),
                claimedChests);
        WorkspaceProjectionRequest seed = storageRequestWithResolverAndProximateIds(
                authority,
                workflow,
                storageIndex(List.of(claimedStorageEntry(storageId, "minecraft:dirt", 64, true)), 1L),
                Set.of(storageId.toString()));
        WorkspaceProjectionRequest changed = storageRequestWithResolverAndProximateIds(
                authority,
                workflow,
                storageIndex(List.of(claimedEmptyStorageEntry(storageId, true)), 2L),
                Set.of(storageId.toString()));
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED,
                Set.of(),
                Set.of(storageId.toString()),
                Set.of(),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.STORAGE,
                        WorkspaceProjectionSlice.WORKFLOW,
                        WorkspaceProjectionSlice.CONTEXTUAL,
                        WorkspaceProjectionSlice.WAYFINDING,
                        WorkspaceProjectionSlice.FRAME),
                false,
                "simple_workflow_storage_put_away_route_remove");
        SlotWorkspaceViewModel.setGhostStackResolver(itemId -> new ItemStack(itemId, 1, 64));

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.AtlasItem dirtCard = card(result.viewModel(), "minecraft:dirt");
        SlotWorkspaceViewModel.ContextualSuggestionLane putAwayLane = result.viewModel().contextualSuggestionLanes()
                .stream()
                .filter(SlotWorkspaceViewModel.ContextualSuggestionLane::putAway)
                .findFirst()
                .orElseThrow();

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertTrue(dirtCard.carried());
        assertEquals(SlotWorkspaceViewModel.PutAwayState.NO_ROUTE, dirtCard.putAwayState());
        assertEquals(1, putAwayLane.items().size());
        assertEquals(SlotWorkspaceViewModel.PutAwayState.NO_ROUTE, putAwayLane.items().get(0).putAwayState());
        assertTrue(result.viewModel().wayfindingTargets().isEmpty());
        assertEquals("kit-1", result.viewModel().activeKit().kitId());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleWorkflowTrackedDisplayPutAwayActivationInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity hammer = new ItemIdentity("gtceu:steel_mining_hammer", ItemComparisonMode.ITEM_ID, "");
        ItemIdentity dirt = new ItemIdentity("minecraft:dirt", ItemComparisonMode.ITEM_ID, "");
        String storageId = "tool-rack-dirt";
        KitDefinition kit = new KitDefinition(
                "kit-1",
                "Mining",
                List.of(KitPage.empty().withSlot(0, hammer)),
                null);
        InventoryAuthoritySnapshot authority = authority(List.of(stack(4, "minecraft:dirt", 32)));
        WorkflowDomainSnapshot seedWorkflow = workflowWithKitMap(
                VisualHomeMap.empty(),
                new KitMap(List.of(kit), KitActivation.NONE));
        WorkflowDomainSnapshot changedWorkflow = workflowWithKitMap(
                VisualHomeMap.empty(),
                new KitMap(List.of(kit), new KitActivation(kit.id(), 0, Set.of(dirt))));
        WorkspaceStorageIndex index = storageIndex(
                List.of(remoteDisplayStorageEntry(storageId, 7, "minecraft:dirt", 64)),
                1L);
        WorkspaceProjectionRequest seed = storageRequestWithResolver(
                authority,
                seedWorkflow,
                index);
        WorkspaceProjectionRequest changed = storageRequestWithResolver(
                authority,
                changedWorkflow,
                index);
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.WORKFLOW_SEQUENCE_CHANGED,
                Set.of(hammer, dirt),
                Set.of(),
                Set.of(),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.WORKFLOW,
                        WorkspaceProjectionSlice.CONTEXTUAL,
                        WorkspaceProjectionSlice.WAYFINDING,
                        WorkspaceProjectionSlice.FRAME),
                false,
                "simple_workflow_tracked_display_put_away_activate");
        SlotWorkspaceViewModel.setGhostStackResolver(itemId -> new ItemStack(itemId, 1, 64));

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.AtlasItem dirtCard = card(result.viewModel(), "minecraft:dirt");
        SlotWorkspaceViewModel.ContextualSuggestionLane putAwayLane = result.viewModel().contextualSuggestionLanes()
                .stream()
                .filter(SlotWorkspaceViewModel.ContextualSuggestionLane::putAway)
                .findFirst()
                .orElseThrow();
        WayfindingTarget target = result.viewModel().wayfindingTargets().get(0);

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertTrue(dirtCard.carried());
        assertFalse(dirtCard.ghost());
        assertEquals(32, dirtCard.totalCount());
        assertEquals(SlotWorkspaceViewModel.PutAwayState.ROUTED, dirtCard.putAwayState());
        assertEquals(1, putAwayLane.items().size());
        assertEquals("minecraft:dirt", putAwayLane.items().get(0).identity().itemId());
        assertEquals(SlotWorkspaceViewModel.PutAwayState.ROUTED, putAwayLane.items().get(0).putAwayState());
        assertEquals(1, result.viewModel().wayfindingTargets().size());
        assertEquals(storageId, target.storageId());
        assertEquals(WayfindingTarget.Scope.PUT_AWAY, target.scope());
        assertTrue(target.putAwayIdentities().contains(dirt));
        assertEquals(32, target.totalMissingCount());
        assertEquals("kit-1", result.viewModel().activeKit().kitId());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleWorkflowTrackedDisplayPutAwayDeactivationInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity hammer = new ItemIdentity("gtceu:steel_mining_hammer", ItemComparisonMode.ITEM_ID, "");
        ItemIdentity dirt = new ItemIdentity("minecraft:dirt", ItemComparisonMode.ITEM_ID, "");
        String storageId = "tool-rack-dirt-off";
        KitDefinition kit = new KitDefinition(
                "kit-1",
                "Mining",
                List.of(KitPage.empty().withSlot(0, hammer)),
                null);
        InventoryAuthoritySnapshot authority = authority(List.of(stack(4, "minecraft:dirt", 32)));
        WorkflowDomainSnapshot seedWorkflow = workflowWithKitMap(
                VisualHomeMap.empty(),
                new KitMap(List.of(kit), new KitActivation(kit.id(), 0, Set.of(dirt))));
        WorkflowDomainSnapshot changedWorkflow = workflowWithKitMap(
                VisualHomeMap.empty(),
                new KitMap(List.of(kit), KitActivation.NONE));
        WorkspaceStorageIndex index = storageIndex(
                List.of(remoteDisplayStorageEntry(storageId, 8, "minecraft:dirt", 64)),
                1L);
        WorkspaceProjectionRequest seed = storageRequestWithResolver(
                authority,
                seedWorkflow,
                index);
        WorkspaceProjectionRequest changed = storageRequestWithResolver(
                authority,
                changedWorkflow,
                index);
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.WORKFLOW_SEQUENCE_CHANGED,
                Set.of(hammer, dirt),
                Set.of(),
                Set.of(),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.WORKFLOW,
                        WorkspaceProjectionSlice.CONTEXTUAL,
                        WorkspaceProjectionSlice.WAYFINDING,
                        WorkspaceProjectionSlice.FRAME),
                false,
                "simple_workflow_tracked_display_put_away_deactivate");
        SlotWorkspaceViewModel.setGhostStackResolver(itemId -> new ItemStack(itemId, 1, 64));

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.AtlasItem dirtCard = card(result.viewModel(), "minecraft:dirt");

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertFalse(hasCard(result.viewModel(), "gtceu:steel_mining_hammer"));
        assertTrue(dirtCard.carried());
        assertFalse(dirtCard.ghost());
        assertEquals(32, dirtCard.totalCount());
        assertEquals(SlotWorkspaceViewModel.PutAwayState.NONE, dirtCard.putAwayState());
        assertTrue(result.viewModel().contextualSuggestionLanes().stream().noneMatch(
                SlotWorkspaceViewModel.ContextualSuggestionLane::putAway));
        assertTrue(result.viewModel().wayfindingTargets().isEmpty());
        assertNull(result.viewModel().activeKit());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleWorkflowExactAcceptedActivationInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity ore = new ItemIdentity("minecraft:iron_ore", ItemComparisonMode.ITEM_ID, "");
        KitDefinition kit = new KitDefinition(
                "kit-1",
                "Smelting",
                List.of(KitPage.empty()),
                null,
                "",
                Set.of(),
                Set.of(WorkflowAcceptedInputRule.exact(ore)));
        InventoryAuthoritySnapshot authority = authority(List.of(stack(2, "minecraft:iron_ore", 8)));
        WorkflowDomainSnapshot seedWorkflow = workflowWithKitMap(
                VisualHomeMap.empty(),
                new KitMap(List.of(kit), KitActivation.NONE));
        WorkflowDomainSnapshot changedWorkflow = workflowWithKitMap(
                VisualHomeMap.empty(),
                new KitMap(List.of(kit), new KitActivation(kit.id(), 0)));
        WorkspaceProjectionRequest seed = storageRequest(
                authority,
                seedWorkflow,
                WorkspaceStorageIndex.empty());
        WorkspaceProjectionRequest changed = storageRequest(
                authority,
                changedWorkflow,
                WorkspaceStorageIndex.empty());
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.WORKFLOW_SEQUENCE_CHANGED,
                Set.of(ore),
                Set.of(),
                Set.of(),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.WORKFLOW,
                        WorkspaceProjectionSlice.CONTEXTUAL,
                        WorkspaceProjectionSlice.WAYFINDING,
                        WorkspaceProjectionSlice.FRAME),
                false,
                "simple_workflow_exact_accepted_activate");

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.AtlasItem oreCard = card(result.viewModel(), "minecraft:iron_ore");

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertTrue(oreCard.carried());
        assertFalse(oreCard.ghost());
        assertTrue(oreCard.acceptedWorkflowInput());
        assertEquals(0, oreCard.desiredCount());
        assertEquals(0, oreCard.wantedCount());
        assertFalse(oreCard.kitNeeded());
        assertEquals(SlotWorkspaceViewModel.PutAwayState.NONE, oreCard.putAwayState());
        assertTrue(result.viewModel().contextualSuggestionLanes().isEmpty());
        assertEquals("kit-1", result.viewModel().activeKit().kitId());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleWorkflowExactAcceptedDeactivationInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity ore = new ItemIdentity("minecraft:iron_ore", ItemComparisonMode.ITEM_ID, "");
        KitDefinition kit = new KitDefinition(
                "kit-1",
                "Smelting",
                List.of(KitPage.empty()),
                null,
                "",
                Set.of(),
                Set.of(WorkflowAcceptedInputRule.exact(ore)));
        InventoryAuthoritySnapshot authority = authority(List.of(stack(2, "minecraft:iron_ore", 8)));
        WorkflowDomainSnapshot seedWorkflow = workflowWithKitMap(
                VisualHomeMap.empty(),
                new KitMap(List.of(kit), new KitActivation(kit.id(), 0)));
        WorkflowDomainSnapshot changedWorkflow = workflowWithKitMap(
                VisualHomeMap.empty(),
                new KitMap(List.of(kit), KitActivation.NONE));
        WorkspaceProjectionRequest seed = storageRequest(
                authority,
                seedWorkflow,
                WorkspaceStorageIndex.empty());
        WorkspaceProjectionRequest changed = storageRequest(
                authority,
                changedWorkflow,
                WorkspaceStorageIndex.empty());
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.WORKFLOW_SEQUENCE_CHANGED,
                Set.of(ore),
                Set.of(),
                Set.of(),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.WORKFLOW,
                        WorkspaceProjectionSlice.CONTEXTUAL,
                        WorkspaceProjectionSlice.WAYFINDING,
                        WorkspaceProjectionSlice.FRAME),
                false,
                "simple_workflow_exact_accepted_deactivate");

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.AtlasItem oreCard = card(result.viewModel(), "minecraft:iron_ore");

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertTrue(oreCard.carried());
        assertFalse(oreCard.ghost());
        assertFalse(oreCard.acceptedWorkflowInput());
        assertEquals(0, oreCard.desiredCount());
        assertEquals(0, oreCard.wantedCount());
        assertFalse(oreCard.kitNeeded());
        assertEquals(SlotWorkspaceViewModel.PutAwayState.NONE, oreCard.putAwayState());
        assertTrue(result.viewModel().contextualSuggestionLanes().isEmpty());
        assertNull(result.viewModel().activeKit());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleWorkflowExactAcceptedWithTargetPressureActivationInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity ore = new ItemIdentity("minecraft:iron_ore", ItemComparisonMode.ITEM_ID, "");
        UUID storageId = UUID.fromString("00000000-0000-0000-0000-000000000091");
        ClaimedChest chest = claimedChest(storageId);
        ClaimedChestMap claimedChests = new ClaimedChestMap(List.of(chest));
        KitDefinition kit = new KitDefinition(
                "kit-1",
                "Smelting",
                List.of(KitPage.empty()),
                null,
                "",
                Set.of(),
                Set.of(WorkflowAcceptedInputRule.exact(ore)));
        Map<String, Map<ItemIdentity, Integer>> desired = Map.of(kit.id(), Map.of(ore, 12));
        WorkflowDomainSnapshot seedWorkflow = workflowWithKitMapCountsAndChests(
                VisualHomeMap.empty(),
                new KitMap(List.of(kit), KitActivation.NONE),
                desired,
                Map.of(),
                claimedChests);
        WorkflowDomainSnapshot changedWorkflow = workflowWithKitMapCountsAndChests(
                VisualHomeMap.empty(),
                new KitMap(List.of(kit), new KitActivation(kit.id(), 0)),
                desired,
                Map.of(),
                claimedChests);
        WorkspaceStorageIndex index = storageIndex(
                List.of(claimedStorageEntry(storageId, "minecraft:iron_ore", 9, true)),
                1L);
        WorkspaceProjectionRequest seed = storageRequestWithResolverAndProximateIds(
                InventoryAuthoritySnapshot.empty(),
                seedWorkflow,
                index,
                Set.of(storageId.toString()));
        WorkspaceProjectionRequest changed = storageRequestWithResolverAndProximateIds(
                InventoryAuthoritySnapshot.empty(),
                changedWorkflow,
                index,
                Set.of(storageId.toString()));
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.WORKFLOW_SEQUENCE_CHANGED,
                Set.of(ore),
                Set.of(),
                Set.of(),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.WORKFLOW,
                        WorkspaceProjectionSlice.CONTEXTUAL,
                        WorkspaceProjectionSlice.WAYFINDING,
                        WorkspaceProjectionSlice.FRAME),
                false,
                "simple_workflow_exact_accepted_target_pressure_activate");
        SlotWorkspaceViewModel.setGhostStackResolver(itemId -> new ItemStack(itemId, 1, 64));

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.AtlasItem oreCard = card(result.viewModel(), "minecraft:iron_ore");
        WayfindingTarget target = result.viewModel().wayfindingTargets().get(0);

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertTrue(oreCard.ghost());
        assertTrue(oreCard.acceptedWorkflowInput());
        assertTrue(oreCard.kitNeeded());
        assertEquals(12, oreCard.desiredCount());
        assertTrue(oreCard.desiredCountFromKit());
        assertEquals(9, oreCard.totalCount());
        assertEquals(9, oreCard.proximateCount());
        assertEquals(1, result.viewModel().wayfindingTargets().size());
        assertEquals(storageId.toString(), target.storageId());
        assertEquals(WayfindingTarget.Scope.KIT, target.scope());
        assertTrue(target.kitMissingIdentities().contains(ore));
        assertTrue(target.desiredMissingIdentities().contains(ore));
        assertEquals(9, target.totalMissingCount());
        assertEquals("kit-1", result.viewModel().activeKit().kitId());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleWorkflowExactAcceptedWithTargetPressureDeactivationInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity ore = new ItemIdentity("minecraft:iron_ore", ItemComparisonMode.ITEM_ID, "");
        UUID storageId = UUID.fromString("00000000-0000-0000-0000-000000000092");
        ClaimedChest chest = claimedChest(storageId);
        ClaimedChestMap claimedChests = new ClaimedChestMap(List.of(chest));
        KitDefinition kit = new KitDefinition(
                "kit-1",
                "Smelting",
                List.of(KitPage.empty()),
                null,
                "",
                Set.of(),
                Set.of(WorkflowAcceptedInputRule.exact(ore)));
        Map<String, Map<ItemIdentity, Integer>> desired = Map.of(kit.id(), Map.of(ore, 12));
        WorkflowDomainSnapshot seedWorkflow = workflowWithKitMapCountsAndChests(
                VisualHomeMap.empty(),
                new KitMap(List.of(kit), new KitActivation(kit.id(), 0)),
                desired,
                Map.of(),
                claimedChests);
        WorkflowDomainSnapshot changedWorkflow = workflowWithKitMapCountsAndChests(
                VisualHomeMap.empty(),
                new KitMap(List.of(kit), KitActivation.NONE),
                desired,
                Map.of(),
                claimedChests);
        WorkspaceStorageIndex index = storageIndex(
                List.of(claimedStorageEntry(storageId, "minecraft:iron_ore", 9, true)),
                1L);
        WorkspaceProjectionRequest seed = storageRequestWithResolverAndProximateIds(
                InventoryAuthoritySnapshot.empty(),
                seedWorkflow,
                index,
                Set.of(storageId.toString()));
        WorkspaceProjectionRequest changed = storageRequestWithResolverAndProximateIds(
                InventoryAuthoritySnapshot.empty(),
                changedWorkflow,
                index,
                Set.of(storageId.toString()));
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.WORKFLOW_SEQUENCE_CHANGED,
                Set.of(ore),
                Set.of(),
                Set.of(),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.WORKFLOW,
                        WorkspaceProjectionSlice.CONTEXTUAL,
                        WorkspaceProjectionSlice.WAYFINDING,
                        WorkspaceProjectionSlice.FRAME),
                false,
                "simple_workflow_exact_accepted_target_pressure_deactivate");
        SlotWorkspaceViewModel.setGhostStackResolver(itemId -> new ItemStack(itemId, 1, 64));

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.AtlasItem oreCard = card(result.viewModel(), "minecraft:iron_ore");

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertTrue(oreCard.ghost());
        assertFalse(oreCard.acceptedWorkflowInput());
        assertFalse(oreCard.kitNeeded());
        assertEquals(0, oreCard.desiredCount());
        assertFalse(oreCard.desiredCountFromKit());
        assertEquals(9, oreCard.totalCount());
        assertEquals(9, oreCard.proximateCount());
        assertTrue(result.viewModel().wayfindingTargets().isEmpty());
        assertNull(result.viewModel().activeKit());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleWorkflowTagAcceptedActivationInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity coke = new ItemIdentity("tfc:coke", ItemComparisonMode.ITEM_ID, "");
        UUID storageId = UUID.fromString("00000000-0000-0000-0000-000000000094");
        ClaimedChest chest = claimedChest(storageId);
        ClaimedChestMap claimedChests = new ClaimedChestMap(List.of(chest));
        WorkspaceStorageIndex index = storageIndex(
                List.of(claimedStorageEntryWithTags(
                        storageId,
                        "tfc:coke",
                        16,
                        true,
                        "tfc:blast_furnace_fuel")),
                1L);
        KitDefinition kit = new KitDefinition(
                "kit-1",
                "Smelting",
                List.of(KitPage.empty()),
                null,
                "",
                Set.of(),
                Set.of(WorkflowAcceptedInputRule.itemTag("tfc:blast_furnace_fuel")));
        WorkflowDomainSnapshot seedWorkflow = workflowWithKitMapAndChests(
                homeMap(coke),
                new KitMap(List.of(kit), KitActivation.NONE),
                claimedChests);
        WorkflowDomainSnapshot changedWorkflow = workflowWithKitMapAndChests(
                homeMap(coke),
                new KitMap(List.of(kit), new KitActivation(kit.id(), 0)),
                claimedChests);
        WorkspaceProjectionRequest seed = storageRequestWithResolverAndProximateIds(
                InventoryAuthoritySnapshot.empty(),
                seedWorkflow,
                index,
                Set.of(storageId.toString()));
        WorkspaceProjectionRequest changed = storageRequestWithResolverAndProximateIds(
                InventoryAuthoritySnapshot.empty(),
                changedWorkflow,
                index,
                Set.of(storageId.toString()));
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.WORKFLOW_SEQUENCE_CHANGED,
                Set.of(),
                Set.of(),
                Set.of(),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.WORKFLOW,
                        WorkspaceProjectionSlice.CONTEXTUAL,
                        WorkspaceProjectionSlice.WAYFINDING,
                        WorkspaceProjectionSlice.FRAME),
                false,
                "workflow_accepted_input_tag_scope_changed");

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.AtlasItem cokeCard = card(result.viewModel(), "tfc:coke");

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertTrue(cokeCard.ghost());
        assertTrue(cokeCard.acceptedWorkflowInput());
        assertFalse(cokeCard.kitNeeded());
        assertEquals(0, cokeCard.desiredCount());
        assertEquals(0, cokeCard.wantedCount());
        assertEquals(16, cokeCard.totalCount());
        assertEquals(16, cokeCard.proximateCount());
        assertEquals("kit-1", result.viewModel().activeKit().kitId());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleWorkflowTagAcceptedDeactivationInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity coke = new ItemIdentity("tfc:coke", ItemComparisonMode.ITEM_ID, "");
        UUID storageId = UUID.fromString("00000000-0000-0000-0000-000000000095");
        ClaimedChest chest = claimedChest(storageId);
        ClaimedChestMap claimedChests = new ClaimedChestMap(List.of(chest));
        WorkspaceStorageIndex index = storageIndex(
                List.of(claimedStorageEntryWithTags(
                        storageId,
                        "tfc:coke",
                        16,
                        true,
                        "tfc:blast_furnace_fuel")),
                1L);
        KitDefinition kit = new KitDefinition(
                "kit-1",
                "Smelting",
                List.of(KitPage.empty()),
                null,
                "",
                Set.of(),
                Set.of(WorkflowAcceptedInputRule.itemTag("tfc:blast_furnace_fuel")));
        WorkflowDomainSnapshot seedWorkflow = workflowWithKitMapAndChests(
                homeMap(coke),
                new KitMap(List.of(kit), new KitActivation(kit.id(), 0)),
                claimedChests);
        WorkflowDomainSnapshot changedWorkflow = workflowWithKitMapAndChests(
                homeMap(coke),
                new KitMap(List.of(kit), KitActivation.NONE),
                claimedChests);
        WorkspaceProjectionRequest seed = storageRequestWithResolverAndProximateIds(
                InventoryAuthoritySnapshot.empty(),
                seedWorkflow,
                index,
                Set.of(storageId.toString()));
        WorkspaceProjectionRequest changed = storageRequestWithResolverAndProximateIds(
                InventoryAuthoritySnapshot.empty(),
                changedWorkflow,
                index,
                Set.of(storageId.toString()));
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.WORKFLOW_SEQUENCE_CHANGED,
                Set.of(),
                Set.of(),
                Set.of(),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.WORKFLOW,
                        WorkspaceProjectionSlice.CONTEXTUAL,
                        WorkspaceProjectionSlice.WAYFINDING,
                        WorkspaceProjectionSlice.FRAME),
                false,
                "workflow_accepted_input_tag_scope_changed");

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.AtlasItem cokeCard = card(result.viewModel(), "tfc:coke");

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertTrue(cokeCard.ghost());
        assertFalse(cokeCard.acceptedWorkflowInput());
        assertFalse(cokeCard.kitNeeded());
        assertEquals(0, cokeCard.desiredCount());
        assertEquals(0, cokeCard.wantedCount());
        assertEquals(16, cokeCard.totalCount());
        assertEquals(16, cokeCard.proximateCount());
        assertNull(result.viewModel().activeKit());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleWorkflowActivationWithClaimedStorageWayfindingInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity hammer = new ItemIdentity("gtceu:steel_mining_hammer", ItemComparisonMode.ITEM_ID, "");
        UUID storageId = UUID.fromString("00000000-0000-0000-0000-000000000085");
        ClaimedChest chest = claimedChest(storageId);
        KitDefinition kit = new KitDefinition(
                "kit-1",
                "Mining",
                List.of(KitPage.empty().withSlot(0, hammer)),
                null);
        ClaimedChestMap claimedChests = new ClaimedChestMap(List.of(chest));
        WorkflowDomainSnapshot seedWorkflow = workflowWithKitMapAndChests(
                VisualHomeMap.empty(),
                new KitMap(List.of(kit), KitActivation.NONE),
                claimedChests);
        WorkflowDomainSnapshot changedWorkflow = workflowWithKitMapAndChests(
                VisualHomeMap.empty(),
                new KitMap(List.of(kit), new KitActivation(kit.id(), 0)),
                claimedChests);
        WorkspaceStorageIndex index = storageIndex(
                List.of(claimedStorageEntry(storageId, "gtceu:steel_mining_hammer", 9, true)),
                1L);
        WorkspaceProjectionRequest seed = storageRequestWithResolverAndProximateIds(
                InventoryAuthoritySnapshot.empty(),
                seedWorkflow,
                index,
                Set.of(storageId.toString()));
        WorkspaceProjectionRequest changed = storageRequestWithResolverAndProximateIds(
                InventoryAuthoritySnapshot.empty(),
                changedWorkflow,
                index,
                Set.of(storageId.toString()));
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.WORKFLOW_SEQUENCE_CHANGED,
                Set.of(hammer),
                Set.of(),
                Set.of(),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.WORKFLOW,
                        WorkspaceProjectionSlice.CONTEXTUAL,
                        WorkspaceProjectionSlice.WAYFINDING,
                        WorkspaceProjectionSlice.FRAME),
                false,
                "simple_workflow_activate_claimed_storage_wayfinding");
        SlotWorkspaceViewModel.setGhostStackResolver(itemId -> new ItemStack(itemId, 1, 64));

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.AtlasItem ghost = card(result.viewModel(), "gtceu:steel_mining_hammer");
        WayfindingTarget target = result.viewModel().wayfindingTargets().get(0);

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertTrue(ghost.ghost());
        assertTrue(ghost.kitNeeded());
        assertEquals(9, ghost.totalCount());
        assertEquals(9, ghost.proximateCount());
        assertEquals(1, ghost.presence().size());
        assertEquals(storageId.toString(), ghost.presence().get(0).storageId());
        assertEquals(1, result.viewModel().wayfindingTargets().size());
        assertEquals(storageId.toString(), target.storageId());
        assertEquals(WayfindingTarget.Scope.KIT, target.scope());
        assertTrue(target.kitMissingIdentities().contains(hammer));
        assertEquals(9, target.totalMissingCount());
        assertEquals("kit-1", result.viewModel().activeKit().kitId());
        assertEquals("cluster-" + storageId, chip(result.viewModel(), storageId.toString()).clusterId());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleWorkflowDeactivationWithClaimedStorageWayfindingInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity hammer = new ItemIdentity("gtceu:steel_mining_hammer", ItemComparisonMode.ITEM_ID, "");
        UUID storageId = UUID.fromString("00000000-0000-0000-0000-000000000086");
        ClaimedChest chest = claimedChest(storageId);
        KitDefinition kit = new KitDefinition(
                "kit-1",
                "Mining",
                List.of(KitPage.empty().withSlot(0, hammer)),
                null);
        ClaimedChestMap claimedChests = new ClaimedChestMap(List.of(chest));
        WorkflowDomainSnapshot seedWorkflow = workflowWithKitMapAndChests(
                VisualHomeMap.empty(),
                new KitMap(List.of(kit), new KitActivation(kit.id(), 0)),
                claimedChests);
        WorkflowDomainSnapshot changedWorkflow = workflowWithKitMapAndChests(
                VisualHomeMap.empty(),
                new KitMap(List.of(kit), KitActivation.NONE),
                claimedChests);
        WorkspaceStorageIndex index = storageIndex(
                List.of(claimedStorageEntry(storageId, "gtceu:steel_mining_hammer", 9, true)),
                1L);
        WorkspaceProjectionRequest seed = storageRequestWithResolverAndProximateIds(
                InventoryAuthoritySnapshot.empty(),
                seedWorkflow,
                index,
                Set.of(storageId.toString()));
        WorkspaceProjectionRequest changed = storageRequestWithResolverAndProximateIds(
                InventoryAuthoritySnapshot.empty(),
                changedWorkflow,
                index,
                Set.of(storageId.toString()));
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.WORKFLOW_SEQUENCE_CHANGED,
                Set.of(hammer),
                Set.of(),
                Set.of(),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.WORKFLOW,
                        WorkspaceProjectionSlice.CONTEXTUAL,
                        WorkspaceProjectionSlice.WAYFINDING,
                        WorkspaceProjectionSlice.FRAME),
                false,
                "simple_workflow_deactivate_claimed_storage_wayfinding");
        SlotWorkspaceViewModel.setGhostStackResolver(itemId -> new ItemStack(itemId, 1, 64));

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.AtlasItem ghost = card(result.viewModel(), "gtceu:steel_mining_hammer");

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertTrue(ghost.ghost());
        assertFalse(ghost.kitNeeded());
        assertEquals(0, ghost.wantedCount());
        assertEquals(9, ghost.totalCount());
        assertEquals(9, ghost.proximateCount());
        assertEquals(1, ghost.presence().size());
        assertEquals(storageId.toString(), ghost.presence().get(0).storageId());
        assertTrue(result.viewModel().wayfindingTargets().isEmpty());
        assertNull(result.viewModel().activeKit());
        assertEquals("cluster-" + storageId, chip(result.viewModel(), storageId.toString()).clusterId());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleRemoteWantedTargetStorageInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity stone = new ItemIdentity("minecraft:stone", ItemComparisonMode.ITEM_ID, "");
        WorkspaceProjectionRequest seed = storageRequestWithResolver(
                InventoryAuthoritySnapshot.empty(),
                workflowWithHomes(VisualHomeMap.empty()),
                storageIndex(List.of(), 1L));
        WorkspaceProjectionRequest changed = storageRequestWithResolver(
                InventoryAuthoritySnapshot.empty(),
                workflowWithHomesAndTargets(VisualHomeMap.empty(), Map.of(), Map.of(stone, 32)),
                storageIndex(List.of(remoteDisplayStorageEntry("storage-a", 1, "minecraft:stone", 9)), 2L));
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.COMMAND_OUTCOME,
                Set.of(stone),
                Set.of("storage-a"),
                Set.of(),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.STORAGE,
                        WorkspaceProjectionSlice.WAYFINDING,
                        WorkspaceProjectionSlice.WORKFLOW),
                false,
                "simple_remote_wanted_target_storage");

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.AtlasItem ghost = card(result.viewModel(), "minecraft:stone");
        WayfindingTarget target = result.viewModel().wayfindingTargets().get(0);

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos());
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertFalse(ghost.carried());
        assertTrue(ghost.ghost());
        assertEquals(9, ghost.totalCount());
        assertEquals(32, ghost.wantedCount());
        assertEquals(1, ghost.elsewhere().size());
        assertEquals("storage-a", ghost.elsewhere().get(0).storageId());
        assertEquals(9, ghost.elsewhere().get(0).count());
        assertEquals(1, result.viewModel().wayfindingTargets().size());
        assertEquals("storage-a", target.storageId());
        assertEquals(WayfindingTarget.Scope.WANTED, target.scope());
        assertTrue(target.wantedMissingIdentities().contains(stone));
        assertEquals(9, target.totalMissingCount());
        assertEquals(9, chip(result.viewModel(), "storage-a").contents().get(0).count());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleRemoteWantedCarriedTargetStorageInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity stone = new ItemIdentity("minecraft:stone", ItemComparisonMode.ITEM_ID, "");
        WorkflowDomainSnapshot workflow = workflowWithHomesAndTargets(
                VisualHomeMap.empty(),
                Map.of(),
                Map.of(stone, 32));
        WorkspaceProjectionRequest seed = storageRequestWithResolver(
                authority("minecraft:stone", 4),
                workflow,
                storageIndex(List.of(), 1L));
        WorkspaceProjectionRequest changed = storageRequestWithResolver(
                authority("minecraft:stone", 4),
                workflow,
                storageIndex(List.of(remoteDisplayStorageEntry("storage-a", 1, "minecraft:stone", 9)), 2L));
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED,
                Set.of(stone),
                Set.of("storage-a"),
                Set.of(),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.STORAGE,
                        WorkspaceProjectionSlice.WAYFINDING,
                        WorkspaceProjectionSlice.WORKFLOW),
                false,
                "simple_remote_wanted_carried_target_storage");

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.AtlasItem card = card(result.viewModel(), "minecraft:stone");
        WayfindingTarget target = result.viewModel().wayfindingTargets().get(0);

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos());
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertTrue(card.carried());
        assertFalse(card.ghost());
        assertEquals(4, card.totalCount());
        assertEquals(32, card.wantedCount());
        assertEquals(1, card.elsewhere().size());
        assertEquals(9, card.elsewhere().get(0).count());
        assertEquals(1, result.viewModel().wayfindingTargets().size());
        assertEquals("storage-a", target.storageId());
        assertEquals(WayfindingTarget.Scope.WANTED, target.scope());
        assertTrue(target.wantedMissingIdentities().contains(stone));
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleProximateWantedTargetDisplayStorageInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity stone = new ItemIdentity("minecraft:stone", ItemComparisonMode.ITEM_ID, "");
        WorldDisplayStorageSource source = displayStorageSource("storage-a", 1, "minecraft:stone", 9);
        WorkspaceProjectionRequest seed = storageRequestWithResolverAndDisplaySources(
                InventoryAuthoritySnapshot.empty(),
                workflowWithHomes(VisualHomeMap.empty()),
                storageIndex(List.of(), 1L));
        WorkspaceProjectionRequest changed = storageRequestWithResolverAndDisplaySources(
                InventoryAuthoritySnapshot.empty(),
                workflowWithHomesAndTargets(VisualHomeMap.empty(), Map.of(), Map.of(stone, 32)),
                displayStorageIndex(List.of(source), 2L));
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED,
                Set.of(stone),
                Set.of(source.storageId()),
                Set.of(),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.STORAGE,
                        WorkspaceProjectionSlice.WAYFINDING,
                        WorkspaceProjectionSlice.WORKFLOW),
                false,
                "simple_proximate_wanted_target_display_storage");

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.AtlasItem ghost = card(result.viewModel(), "minecraft:stone");
        WayfindingTarget target = result.viewModel().wayfindingTargets().get(0);

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertFalse(ghost.carried());
        assertTrue(ghost.ghost());
        assertEquals(SlotWorkspaceAtlasLayout.ISLAND_TRIAGE, ghost.islandId());
        assertEquals(9, ghost.totalCount());
        assertEquals(9, ghost.proximateCount());
        assertEquals(32, ghost.wantedCount());
        assertEquals(1, ghost.presence().size());
        assertEquals(source.storageId(), ghost.presence().get(0).storageId());
        assertTrue(ghost.elsewhere().isEmpty());
        assertEquals(1, result.viewModel().wayfindingTargets().size());
        assertEquals(source.storageId(), target.storageId());
        assertEquals(WayfindingTarget.Scope.WANTED, target.scope());
        assertTrue(target.wantedMissingIdentities().contains(stone));
        assertTrue(chip(result.viewModel(), source.storageId()).proximate());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleProximateWantedTargetClaimedStorageInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity stone = new ItemIdentity("minecraft:stone", ItemComparisonMode.ITEM_ID, "");
        UUID storageId = UUID.fromString("00000000-0000-0000-0000-000000000077");
        ClaimedChest chest = claimedChest(storageId);
        WorkflowDomainSnapshot seedWorkflow = workflowWithHomesAndTargetsAndChests(
                VisualHomeMap.empty(),
                Map.of(),
                Map.of(stone, 32),
                new ClaimedChestMap(List.of(chest)));
        WorkspaceProjectionRequest seed = storageRequestWithResolverAndProximateIds(
                InventoryAuthoritySnapshot.empty(),
                seedWorkflow,
                storageIndex(List.of(), 1L),
                Set.of());
        WorkspaceProjectionRequest changed = storageRequestWithResolverAndProximateIds(
                InventoryAuthoritySnapshot.empty(),
                seedWorkflow,
                storageIndex(List.of(claimedStorageEntry(storageId, "minecraft:stone", 9, true)), 2L),
                Set.of(storageId.toString()));
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED,
                Set.of(stone),
                Set.of(storageId.toString()),
                Set.of(),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.STORAGE,
                        WorkspaceProjectionSlice.WAYFINDING,
                        WorkspaceProjectionSlice.WORKFLOW),
                false,
                "simple_proximate_wanted_target_claimed_storage");

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.AtlasItem ghost = card(result.viewModel(), "minecraft:stone");
        WayfindingTarget target = result.viewModel().wayfindingTargets().get(0);

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertFalse(ghost.carried());
        assertTrue(ghost.ghost());
        assertEquals(SlotWorkspaceAtlasLayout.ISLAND_TRIAGE, ghost.islandId());
        assertEquals(9, ghost.totalCount());
        assertEquals(9, ghost.proximateCount());
        assertEquals(32, ghost.wantedCount());
        assertEquals(1, ghost.presence().size());
        assertEquals(storageId.toString(), ghost.presence().get(0).storageId());
        assertTrue(ghost.elsewhere().isEmpty());
        assertEquals(storageId.toString(), target.storageId());
        assertEquals(WayfindingTarget.Scope.WANTED, target.scope());
        assertTrue(target.wantedMissingIdentities().contains(stone));
        assertTrue(chip(result.viewModel(), storageId.toString()).proximate());
        assertEquals("cluster-" + storageId, chip(result.viewModel(), storageId.toString()).clusterId());
        assertEquals(1, result.viewModel().chestClusters().size());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleClaimedChestCreateStorageOnlyInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity stone = new ItemIdentity("minecraft:stone", ItemComparisonMode.ITEM_ID, "");
        UUID storageId = UUID.fromString("00000000-0000-0000-0000-000000000120");
        ClaimedChest chest = claimedChest(storageId);
        WorkflowDomainSnapshot seedWorkflow = workflowWithHomesAndTargetsAndChests(
                VisualHomeMap.empty(),
                Map.of(),
                Map.of(stone, 32),
                ClaimedChestMap.empty());
        WorkflowDomainSnapshot changedWorkflow = workflowWithHomesAndTargetsAndChests(
                VisualHomeMap.empty(),
                Map.of(),
                Map.of(stone, 32),
                new ClaimedChestMap(List.of(chest)));
        WorkspaceProjectionRequest seed = storageRequestWithResolverAndProximateIds(
                InventoryAuthoritySnapshot.empty(),
                seedWorkflow,
                storageIndex(List.of(), 1L),
                Set.of());
        WorkspaceProjectionRequest changed = storageRequestWithResolverAndProximateIds(
                InventoryAuthoritySnapshot.empty(),
                changedWorkflow,
                storageIndex(List.of(claimedStorageEntry(storageId, "minecraft:stone", 9, true)), 2L),
                Set.of(storageId.toString()));
        WorkspaceInvalidation invalidation =
                SlotWorkspaceCommandService.localizedChestClaimInvalidation(storageId.toString());

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.AtlasItem ghost = card(result.viewModel(), "minecraft:stone");
        WayfindingTarget target = result.viewModel().wayfindingTargets().get(0);

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertTrue(ghost.ghost());
        assertEquals(9, ghost.proximateCount());
        assertEquals(1, ghost.presence().size());
        assertEquals(storageId.toString(), ghost.presence().get(0).storageId());
        assertEquals(storageId.toString(), target.storageId());
        assertEquals(WayfindingTarget.Scope.WANTED, target.scope());
        assertTrue(target.wantedMissingIdentities().contains(stone));
        assertTrue(chip(result.viewModel(), storageId.toString()).proximate());
        assertEquals("cluster-" + storageId, chip(result.viewModel(), storageId.toString()).clusterId());
        assertEquals(1, result.viewModel().chestClusters().size());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleClaimedChestProximityEnterStorageOnlyInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity stone = new ItemIdentity("minecraft:stone", ItemComparisonMode.ITEM_ID, "");
        UUID storageId = UUID.fromString("00000000-0000-0000-0000-000000000083");
        ClaimedChest chest = claimedChest(storageId);
        WorkflowDomainSnapshot workflow = workflowWithHomesAndTargetsAndChests(
                VisualHomeMap.empty(),
                Map.of(),
                Map.of(stone, 32),
                new ClaimedChestMap(List.of(chest)));
        WorkspaceProjectionRequest seed = storageRequestWithResolverAndProximateIds(
                InventoryAuthoritySnapshot.empty(),
                workflow,
                storageIndex(List.of(claimedStorageEntry(storageId, "minecraft:stone", 9, false)), 1L),
                Set.of());
        WorkspaceProjectionRequest changed = storageRequestWithResolverAndProximateIds(
                InventoryAuthoritySnapshot.empty(),
                workflow,
                storageIndex(List.of(claimedStorageEntry(storageId, "minecraft:stone", 9, true)), 2L),
                Set.of(storageId.toString()));
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.PROXIMITY_CHANGED,
                Set.of(),
                Set.of(storageId.toString()),
                Set.of(),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.STORAGE,
                        WorkspaceProjectionSlice.WAYFINDING,
                        WorkspaceProjectionSlice.WORKFLOW,
                        WorkspaceProjectionSlice.FRAME),
                false,
                "simple_claimed_chest_proximity_enter");

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.AtlasItem ghost = card(result.viewModel(), "minecraft:stone");

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertFalse(ghost.carried());
        assertTrue(ghost.ghost());
        assertEquals(9, ghost.proximateCount());
        assertEquals(1, ghost.presence().size());
        assertEquals(storageId.toString(), ghost.presence().get(0).storageId());
        assertTrue(ghost.elsewhere().isEmpty());
        assertTrue(chip(result.viewModel(), storageId.toString()).proximate());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleClaimedChestProximityLeaveStorageOnlyInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity stone = new ItemIdentity("minecraft:stone", ItemComparisonMode.ITEM_ID, "");
        UUID storageId = UUID.fromString("00000000-0000-0000-0000-000000000084");
        ClaimedChest chest = claimedChest(storageId);
        WorkflowDomainSnapshot workflow = workflowWithHomesAndTargetsAndChests(
                VisualHomeMap.empty(),
                Map.of(),
                Map.of(stone, 32),
                new ClaimedChestMap(List.of(chest)));
        WorkspaceProjectionRequest seed = storageRequestWithResolverAndProximateIds(
                InventoryAuthoritySnapshot.empty(),
                workflow,
                storageIndex(List.of(claimedStorageEntry(storageId, "minecraft:stone", 9, true)), 1L),
                Set.of(storageId.toString()));
        WorkspaceProjectionRequest changed = storageRequestWithResolverAndProximateIds(
                InventoryAuthoritySnapshot.empty(),
                workflow,
                storageIndex(List.of(claimedStorageEntry(storageId, "minecraft:stone", 9, false)), 2L),
                Set.of());
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.PROXIMITY_CHANGED,
                Set.of(),
                Set.of(storageId.toString()),
                Set.of(),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.STORAGE,
                        WorkspaceProjectionSlice.WAYFINDING,
                        WorkspaceProjectionSlice.WORKFLOW,
                        WorkspaceProjectionSlice.FRAME),
                false,
                "simple_claimed_chest_proximity_leave");

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.AtlasItem ghost = card(result.viewModel(), "minecraft:stone");

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertFalse(ghost.carried());
        assertTrue(ghost.ghost());
        assertEquals(0, ghost.proximateCount());
        assertTrue(ghost.presence().isEmpty());
        assertEquals(1, ghost.elsewhere().size());
        assertEquals(storageId.toString(), ghost.elsewhere().get(0).storageId());
        assertFalse(chip(result.viewModel(), storageId.toString()).proximate());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleProximateClaimedStorageDepositabilityInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity redstone = new ItemIdentity("minecraft:redstone", ItemComparisonMode.ITEM_ID, "");
        UUID storageId = UUID.fromString("00000000-0000-0000-0000-000000000078");
        ClaimedChest chest = claimedChest(storageId);
        WorkflowDomainSnapshot workflow = workflowWithHomesAndTargetsAndChests(
                homeMap(redstone),
                Map.of(),
                Map.of(),
                new ClaimedChestMap(List.of(chest)));
        WorkspaceProjectionRequest seed = storageRequestWithResolverAndProximateIds(
                authority("minecraft:redstone", 16),
                workflow,
                storageIndex(List.of(), 1L),
                Set.of());
        WorkspaceProjectionRequest changed = storageRequestWithResolverProximateAndDepositIds(
                authority("minecraft:redstone", 16),
                workflow,
                storageIndex(List.of(claimedStorageEntry(storageId, "minecraft:redstone", 32, true)), 2L),
                Set.of(storageId.toString()),
                Set.of(storageId.toString()));
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED,
                Set.of(redstone),
                Set.of(storageId.toString()),
                Set.of(),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.STORAGE,
                        WorkspaceProjectionSlice.WAYFINDING,
                        WorkspaceProjectionSlice.DEPOSITABILITY,
                        WorkspaceProjectionSlice.WORKFLOW),
                false,
                "simple_proximate_claimed_storage_depositability");

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.AtlasItem card = card(result.viewModel(), "minecraft:redstone");

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertTrue(card.carried());
        assertEquals("materials", card.islandId());
        assertEquals(16, card.totalCount());
        assertEquals(32, card.proximateCount());
        assertEquals(1, card.presence().size());
        assertTrue(result.viewModel().depositableIdentities().contains(
                SlotWorkspaceViewModel.IdentityRef.from(redstone)));
        assertTrue(chip(result.viewModel(), storageId.toString()).proximate());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleProximateClaimedStorageAffinityDepositabilityInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity redstone = new ItemIdentity("minecraft:redstone", ItemComparisonMode.ITEM_ID, "");
        UUID storageId = UUID.fromString("00000000-0000-0000-0000-000000000079");
        ClaimedChest chest = claimedChest(storageId);
        WorkflowDomainSnapshot workflow = workflowWithHomesTargetsChestsAndAffinity(
                homeMap(redstone),
                Map.of(),
                Map.of(),
                new ClaimedChestMap(List.of(chest)),
                affinity(storageId, redstone, 1));
        WorkspaceProjectionRequest seed = storageRequestWithResolverProximateAndDepositIds(
                authority("minecraft:redstone", 16),
                workflow,
                storageIndex(List.of(), 1L),
                Set.of(storageId.toString()),
                Set.of(storageId.toString()));
        WorkspaceProjectionRequest changed = storageRequestWithResolverProximateAndDepositIds(
                authority("minecraft:redstone", 16),
                workflow,
                storageIndex(List.of(claimedEmptyStorageEntry(storageId, true)), 2L),
                Set.of(storageId.toString()),
                Set.of(storageId.toString()));
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED,
                Set.of(redstone),
                Set.of(storageId.toString()),
                Set.of(),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.STORAGE,
                        WorkspaceProjectionSlice.WAYFINDING,
                        WorkspaceProjectionSlice.DEPOSITABILITY,
                        WorkspaceProjectionSlice.WORKFLOW),
                false,
                "simple_proximate_claimed_storage_affinity_depositability");

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.AtlasItem card = card(result.viewModel(), "minecraft:redstone");

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertTrue(card.carried());
        assertEquals(0, card.proximateCount());
        assertTrue(result.viewModel().depositableIdentities().contains(
                SlotWorkspaceViewModel.IdentityRef.from(redstone)));
        assertEquals(1, chip(result.viewModel(), storageId.toString()).affinityIdentities());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleAffinityForgetInvalidationClearsDepositabilitySkipsFullProjectionAndMatchesOracle() {
        ItemIdentity redstone = new ItemIdentity("minecraft:redstone", ItemComparisonMode.ITEM_ID, "");
        UUID storageId = UUID.fromString("00000000-0000-0000-0000-000000000084");
        ClaimedChest chest = claimedChest(storageId);
        WorkflowDomainSnapshot seedWorkflow = workflowWithHomesTargetsChestsAndAffinity(
                homeMap(redstone),
                Map.of(),
                Map.of(),
                new ClaimedChestMap(List.of(chest)),
                affinity(storageId, redstone, 1));
        WorkflowDomainSnapshot changedWorkflow = workflowWithHomesTargetsChestsAndAffinity(
                homeMap(redstone),
                Map.of(),
                Map.of(),
                new ClaimedChestMap(List.of(chest)),
                ChestAffinityMap.empty());
        WorkspaceProjectionRequest seed = storageRequestWithResolverProximateAndDepositIds(
                authority("minecraft:redstone", 16),
                seedWorkflow,
                storageIndex(List.of(claimedEmptyStorageEntry(storageId, true)), 1L),
                Set.of(storageId.toString()),
                Set.of(storageId.toString()));
        WorkspaceProjectionRequest changed = storageRequestWithResolverProximateAndDepositIds(
                authority("minecraft:redstone", 16),
                changedWorkflow,
                storageIndex(List.of(claimedEmptyStorageEntry(storageId, true)), 2L),
                Set.of(storageId.toString()),
                Set.of(storageId.toString()));
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.WORKFLOW_SEQUENCE_CHANGED,
                Set.of(redstone),
                Set.of(storageId.toString()),
                Set.of(),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.STORAGE,
                        WorkspaceProjectionSlice.WAYFINDING,
                        WorkspaceProjectionSlice.DEPOSITABILITY,
                        WorkspaceProjectionSlice.WORKFLOW,
                        WorkspaceProjectionSlice.FRAME),
                false,
                "affinity_forgotten");

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertEquals(0, chip(result.viewModel(), storageId.toString()).affinityIdentities());
        assertFalse(result.viewModel().depositableIdentities().contains(
                SlotWorkspaceViewModel.IdentityRef.from(redstone)));
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleProximateClaimedStorageLiveHookDepositabilityInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity redstone = new ItemIdentity("minecraft:redstone", ItemComparisonMode.ITEM_ID, "");
        UUID storageId = UUID.fromString("00000000-0000-0000-0000-000000000080");
        ClaimedChest chest = claimedChest(storageId);
        WorkflowDomainSnapshot workflow = workflowWithHomesAndTargetsAndChests(
                homeMap(redstone),
                Map.of(),
                Map.of(),
                new ClaimedChestMap(List.of(chest)));
        DepositPlanner.ChestContentPresence livePresence = (candidateChest, identity) -> true;
        DepositPlanner.ChestEligibility liveEligibility = candidateChest -> false;
        WorkspaceProjectionRequest seed = storageRequestWithResolverProximateDepositAndLiveHooks(
                authority("minecraft:redstone", 16),
                workflow,
                storageIndex(List.of(), 1L),
                Set.of(),
                Set.of(storageId.toString()),
                livePresence,
                liveEligibility);
        WorkspaceProjectionRequest changed = storageRequestWithResolverProximateDepositAndLiveHooks(
                authority("minecraft:redstone", 16),
                workflow,
                storageIndex(List.of(claimedStorageEntry(storageId, "minecraft:redstone", 32, true)), 2L),
                Set.of(storageId.toString()),
                Set.of(storageId.toString()),
                livePresence,
                liveEligibility);
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED,
                Set.of(redstone),
                Set.of(storageId.toString()),
                Set.of(),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.STORAGE,
                        WorkspaceProjectionSlice.WAYFINDING,
                        WorkspaceProjectionSlice.DEPOSITABILITY,
                        WorkspaceProjectionSlice.WORKFLOW),
                false,
                "simple_proximate_claimed_storage_live_hook_depositability");

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.AtlasItem card = card(result.viewModel(), "minecraft:redstone");

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertEquals(32, card.proximateCount());
        assertFalse(result.viewModel().depositableIdentities().contains(
                SlotWorkspaceViewModel.IdentityRef.from(redstone)));
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleClaimedChestIgnoreRoleInvalidationRemovesStorageAndPresenceSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity redstone = new ItemIdentity("minecraft:redstone", ItemComparisonMode.ITEM_ID, "");
        UUID storageId = UUID.fromString("00000000-0000-0000-0000-000000000081");
        ClaimedChest storageChest = claimedChest(storageId);
        ClaimedChest ignoredChest = storageChest.withRole(ChestRole.IGNORE);
        WorkflowDomainSnapshot seedWorkflow = workflowWithHomesAndTargetsAndChests(
                homeMap(redstone),
                Map.of(),
                Map.of(redstone, 32),
                new ClaimedChestMap(List.of(storageChest)));
        WorkflowDomainSnapshot changedWorkflow = workflowWithHomesAndTargetsAndChests(
                homeMap(redstone),
                Map.of(),
                Map.of(redstone, 32),
                new ClaimedChestMap(List.of(ignoredChest)));
        WorkspaceProjectionRequest seed = storageRequestWithResolverProximateAndDepositIds(
                authority("minecraft:redstone", 16),
                seedWorkflow,
                storageIndex(List.of(claimedStorageEntry(storageId, "minecraft:redstone", 32, true)), 1L),
                Set.of(storageId.toString()),
                Set.of(storageId.toString()));
        WorkspaceProjectionRequest changed = storageRequestWithResolverProximateAndDepositIds(
                authority("minecraft:redstone", 16),
                changedWorkflow,
                storageIndex(List.of(), 2L),
                Set.of(),
                Set.of());
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.WORKFLOW_SEQUENCE_CHANGED,
                Set.of(),
                Set.of(storageId.toString()),
                Set.of(),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.STORAGE,
                        WorkspaceProjectionSlice.WAYFINDING,
                        WorkspaceProjectionSlice.DEPOSITABILITY,
                        WorkspaceProjectionSlice.WORKFLOW,
                        WorkspaceProjectionSlice.REMOTE_SEARCH,
                        WorkspaceProjectionSlice.FRAME),
                false,
                "simple_chest_role_ignore");

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.AtlasItem card = card(result.viewModel(), "minecraft:redstone");

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertNull(result.viewModel().chestChip(storageId.toString()));
        assertTrue(card.carried());
        assertEquals(0, card.proximateCount());
        assertTrue(card.presence().isEmpty());
        assertTrue(card.elsewhere().isEmpty());
        assertTrue(result.viewModel().wayfindingTargets().isEmpty());
        assertTrue(result.viewModel().depositableIdentities().isEmpty());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleClaimedChestBufferRoleInvalidationKeepsStorageButClearsDepositabilitySkipsFullProjectionAndMatchesOracle() {
        ItemIdentity redstone = new ItemIdentity("minecraft:redstone", ItemComparisonMode.ITEM_ID, "");
        UUID storageId = UUID.fromString("00000000-0000-0000-0000-000000000082");
        ClaimedChest storageChest = claimedChest(storageId);
        ClaimedChest bufferChest = storageChest.withRole(ChestRole.BUFFER);
        WorkflowDomainSnapshot seedWorkflow = workflowWithHomesTargetsChestsAndAffinity(
                homeMap(redstone),
                Map.of(),
                Map.of(),
                new ClaimedChestMap(List.of(storageChest)),
                affinity(storageId, redstone, 1));
        WorkflowDomainSnapshot changedWorkflow = workflowWithHomesTargetsChestsAndAffinity(
                homeMap(redstone),
                Map.of(),
                Map.of(),
                new ClaimedChestMap(List.of(bufferChest)),
                ChestAffinityMap.empty());
        WorkspaceProjectionRequest seed = storageRequestWithResolverProximateAndDepositIds(
                authority("minecraft:redstone", 16),
                seedWorkflow,
                storageIndex(List.of(claimedEmptyStorageEntry(storageId, true)), 1L),
                Set.of(storageId.toString()),
                Set.of(storageId.toString()));
        WorkspaceProjectionRequest changed = storageRequestWithResolverProximateAndDepositIds(
                authority("minecraft:redstone", 16),
                changedWorkflow,
                storageIndex(List.of(claimedEmptyStorageEntry(storageId, true)), 2L),
                Set.of(storageId.toString()),
                Set.of());
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.WORKFLOW_SEQUENCE_CHANGED,
                Set.of(),
                Set.of(storageId.toString()),
                Set.of(),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.STORAGE,
                        WorkspaceProjectionSlice.WAYFINDING,
                        WorkspaceProjectionSlice.DEPOSITABILITY,
                        WorkspaceProjectionSlice.WORKFLOW,
                        WorkspaceProjectionSlice.REMOTE_SEARCH,
                        WorkspaceProjectionSlice.FRAME),
                false,
                "simple_chest_role_buffer");

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.AtlasItem card = card(result.viewModel(), "minecraft:redstone");
        SlotWorkspaceViewModel.ChestChip chip = chip(result.viewModel(), storageId.toString());

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertTrue(card.carried());
        assertEquals(0, card.proximateCount());
        assertTrue(chip.proximate());
        assertEquals(0, chip.affinityIdentities());
        assertFalse(result.viewModel().depositableIdentities().contains(
                SlotWorkspaceViewModel.IdentityRef.from(redstone)));
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleClusterRelabelInvalidationUpdatesClusterDescriptorSkipsFullProjectionAndMatchesOracle() {
        UUID storageId = UUID.fromString("00000000-0000-0000-0000-000000000083");
        ClaimedChest chest = claimedChest(storageId);
        ClaimedChestMap claimedChests = new ClaimedChestMap(List.of(chest));
        String clusterId = "cluster-" + storageId;
        WorkflowDomainSnapshot seedWorkflow = workflowWithHomesTargetsChestsAffinityAndClusterLabels(
                homeMap(),
                Map.of(),
                Map.of(),
                claimedChests,
                ChestAffinityMap.empty(),
                Map.of());
        WorkflowDomainSnapshot changedWorkflow = workflowWithHomesTargetsChestsAffinityAndClusterLabels(
                homeMap(),
                Map.of(),
                Map.of(),
                claimedChests,
                ChestAffinityMap.empty(),
                Map.of(clusterId, "Workshop"));
        WorkspaceProjectionRequest seed = storageRequestWithResolverProximateAndDepositIds(
                InventoryAuthoritySnapshot.empty(),
                seedWorkflow,
                storageIndex(List.of(claimedEmptyStorageEntry(storageId, true)), 1L),
                Set.of(storageId.toString()),
                Set.of(storageId.toString()));
        WorkspaceProjectionRequest changed = storageRequestWithResolverProximateAndDepositIds(
                InventoryAuthoritySnapshot.empty(),
                changedWorkflow,
                storageIndex(List.of(claimedEmptyStorageEntry(storageId, true)), 2L),
                Set.of(storageId.toString()),
                Set.of(storageId.toString()));
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.WORKFLOW_SEQUENCE_CHANGED,
                Set.of(),
                Set.of(storageId.toString()),
                Set.of(),
                java.util.EnumSet.of(WorkspaceProjectionSlice.STORAGE, WorkspaceProjectionSlice.FRAME),
                false,
                "cluster_label_changed");

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertEquals("Workshop", cluster(result.viewModel(), clusterId).label());
        assertEquals(clusterId, chip(result.viewModel(), storageId.toString()).clusterId());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleRemoteWantedTargetStorageRemovalSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity stone = new ItemIdentity("minecraft:stone", ItemComparisonMode.ITEM_ID, "");
        WorkflowDomainSnapshot workflow = workflowWithHomesAndTargets(
                VisualHomeMap.empty(),
                Map.of(),
                Map.of(stone, 32));
        WorkspaceProjectionRequest seed = storageRequestWithResolver(
                InventoryAuthoritySnapshot.empty(),
                workflow,
                storageIndex(List.of(remoteDisplayStorageEntry("storage-a", 1, "minecraft:stone", 9)), 1L));
        WorkspaceProjectionRequest changed = storageRequestWithResolver(
                InventoryAuthoritySnapshot.empty(),
                workflow,
                storageIndex(List.of(), 2L));
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED,
                Set.of(stone),
                Set.of("storage-a"),
                Set.of(),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.STORAGE,
                        WorkspaceProjectionSlice.WAYFINDING,
                        WorkspaceProjectionSlice.WORKFLOW),
                false,
                "simple_remote_wanted_target_storage_remove");

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos());
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertFalse(hasCard(result.viewModel(), "minecraft:stone"));
        assertFalse(hasChip(result.viewModel(), "storage-a"));
        assertTrue(result.viewModel().wayfindingTargets().isEmpty());
        assertTrue(result.viewModel().contextualSuggestionLanes().isEmpty());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleTrackedXrayRemoteStorageInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity redstone = new ItemIdentity("minecraft:redstone", ItemComparisonMode.ITEM_ID, "");
        WorkflowDomainSnapshot workflow = workflowWithHomes(homeMap(redstone));
        WorkspaceProjectionRequest seed = storageRequestWithRemoteIntent(
                InventoryAuthoritySnapshot.empty(),
                workflow,
                storageIndex(List.of(), 1L),
                RemoteStorageDetailIntent.TRACKED_XRAY);
        WorkspaceProjectionRequest changed = storageRequestWithRemoteIntent(
                InventoryAuthoritySnapshot.empty(),
                workflow,
                storageIndex(List.of(remoteDisplayStorageEntry("storage-a", 1, "minecraft:redstone", 8)), 2L),
                RemoteStorageDetailIntent.TRACKED_XRAY);
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED,
                Set.of(redstone),
                Set.of("storage-a"),
                Set.of(),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.STORAGE,
                        WorkspaceProjectionSlice.REMOTE_SEARCH),
                false,
                "simple_tracked_xray_remote_storage");

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.AtlasItem ghost = card(result.viewModel(), "minecraft:redstone");

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertFalse(ghost.carried());
        assertTrue(ghost.ghost());
        assertEquals("materials", ghost.islandId());
        assertEquals(8, ghost.totalCount());
        assertEquals(0, ghost.proximateCount());
        assertTrue(ghost.presence().isEmpty());
        assertEquals(1, ghost.elsewhere().size());
        assertEquals("storage-a", ghost.elsewhere().get(0).storageId());
        assertTrue(result.viewModel().wayfindingTargets().isEmpty());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleTrackedXrayUnhomedRemoteStorageInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity redstone = new ItemIdentity("minecraft:redstone", ItemComparisonMode.ITEM_ID, "");
        WorkspaceProjectionRequest seed = storageRequestWithRemoteIntent(
                InventoryAuthoritySnapshot.empty(),
                WorkflowDomainSnapshot.empty(),
                storageIndex(List.of(), 1L),
                RemoteStorageDetailIntent.TRACKED_XRAY);
        WorkspaceProjectionRequest changed = storageRequestWithRemoteIntent(
                InventoryAuthoritySnapshot.empty(),
                WorkflowDomainSnapshot.empty(),
                storageIndex(List.of(remoteDisplayStorageEntry("storage-a", 1, "minecraft:redstone", 8)), 2L),
                RemoteStorageDetailIntent.TRACKED_XRAY);
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED,
                Set.of(redstone),
                Set.of("storage-a"),
                Set.of(),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.STORAGE,
                        WorkspaceProjectionSlice.REMOTE_SEARCH),
                false,
                "simple_tracked_xray_unhomed_remote_storage");

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertFalse(hasCard(result.viewModel(), "minecraft:redstone"));
        assertTrue(result.viewModel().wayfindingTargets().isEmpty());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void simpleTrackedXrayCarriedIdentityInvalidationKeepsElsewherePresenceWithoutFullProjection() {
        ItemIdentity redstone = new ItemIdentity("minecraft:redstone", ItemComparisonMode.ITEM_ID, "");
        WorkflowDomainSnapshot workflow = workflowWithHomes(homeMap(redstone));
        WorkspaceStorageIndex storageIndex = storageIndex(
                List.of(remoteDisplayStorageEntry("storage-a", 1, "minecraft:redstone", 8)),
                1L);
        WorkspaceProjectionRequest seed = storageRequestWithRemoteIntent(
                InventoryAuthoritySnapshot.empty(),
                workflow,
                storageIndex,
                RemoteStorageDetailIntent.TRACKED_XRAY);
        WorkspaceProjectionRequest changed = storageRequestWithRemoteIntent(
                authority("minecraft:redstone", 4),
                workflow,
                storageIndex,
                RemoteStorageDetailIntent.TRACKED_XRAY);
        WorkspaceInvalidation invalidation = WorkspaceInvalidation.localizedIdentity(
                WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED,
                redstone,
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.HOTBAR),
                "simple_tracked_xray_carried_identity");

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.AtlasItem card = card(result.viewModel(), "minecraft:redstone");

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertTrue(card.carried());
        assertFalse(card.ghost());
        assertEquals("materials", card.islandId());
        assertEquals(4, card.totalCount());
        assertEquals(0, card.proximateCount());
        assertTrue(card.presence().isEmpty());
        assertEquals(1, card.elsewhere().size());
        assertEquals("storage-a", card.elsewhere().get(0).storageId());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void visualHomeAssignmentInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity stone = new ItemIdentity("minecraft:stone", ItemComparisonMode.ITEM_ID, "");
        WorkspaceProjectionRequest seed = request(authority(List.of(
                stack(0, "minecraft:stone", 16),
                stack(1, "minecraft:dirt", 4))), WorkflowDomainSnapshot.empty(), "ready", "", 0, 0, "");
        WorkspaceProjectionRequest changed = request(authority(List.of(
                stack(0, "minecraft:stone", 16),
                stack(1, "minecraft:dirt", 4))), workflowWithHomes(homeMap(stone)), "ready", "", 0, 0, "");

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(WorkspaceInvalidation.localizedIdentity(
                        WorkspaceInvalidation.Reason.COMMAND_OUTCOME,
                        stone,
                        java.util.EnumSet.of(WorkspaceProjectionSlice.CARD, WorkspaceProjectionSlice.SECTION),
                        "visual_home_assignment")));
        WorkspaceProjectionResult result = parity.invalidated();

        assertTrue(parity.matches());
        assertEquals(0L, result.diagnostics().timing().projectNanos());
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertEquals("materials", card(result.viewModel(), "minecraft:stone").islandId());
        assertEquals(SlotWorkspaceAtlasLayout.ISLAND_TRIAGE, card(result.viewModel(), "minecraft:dirt").islandId());
        assertEquals(1, result.viewModel().island("materials").carriedCount());
    }

    @Test
    void visualHomeAssignmentWithSectionIdsSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity stone = new ItemIdentity("minecraft:stone", ItemComparisonMode.ITEM_ID, "");
        WorkspaceProjectionRequest seed = request(authority(List.of(
                stack(0, "minecraft:stone", 16),
                stack(1, "minecraft:dirt", 4))), WorkflowDomainSnapshot.empty(), "ready", "", 0, 0, "");
        WorkspaceProjectionRequest changed = request(authority(List.of(
                stack(0, "minecraft:stone", 16),
                stack(1, "minecraft:dirt", 4))), workflowWithHomes(homeMap(stone)), "ready", "", 0, 0, "");
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.COMMAND_OUTCOME,
                Set.of(stone),
                Set.of(),
                Set.of(SlotWorkspaceAtlasLayout.ISLAND_TRIAGE, "materials"),
                java.util.EnumSet.of(WorkspaceProjectionSlice.CARD, WorkspaceProjectionSlice.SECTION),
                false,
                "visual_home_assignment_with_sections");

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();

        assertTrue(parity.matches());
        assertEquals(0L, result.diagnostics().timing().projectNanos());
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertEquals("materials", card(result.viewModel(), "minecraft:stone").islandId());
        assertEquals(1, result.viewModel().island("materials").carriedCount());
    }

    @Test
    void visualHomeClearWithSectionIdsSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity stone = new ItemIdentity("minecraft:stone", ItemComparisonMode.ITEM_ID, "");
        ItemIdentity dirt = new ItemIdentity("minecraft:dirt", ItemComparisonMode.ITEM_ID, "");
        WorkspaceProjectionRequest seed = request(authority(List.of(
                stack(0, "minecraft:stone", 16),
                stack(1, "minecraft:dirt", 4))), workflowWithHomes(homeMap(stone, dirt)), "ready", "", 0, 0, "");
        WorkspaceProjectionRequest changed = request(authority(List.of(
                stack(0, "minecraft:stone", 16),
                stack(1, "minecraft:dirt", 4))), workflowWithHomes(homeMap(dirt)), "ready", "", 0, 0, "");
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.COMMAND_OUTCOME,
                Set.of(stone),
                Set.of(),
                Set.of("materials", SlotWorkspaceAtlasLayout.ISLAND_TRIAGE),
                java.util.EnumSet.of(WorkspaceProjectionSlice.CARD, WorkspaceProjectionSlice.SECTION),
                false,
                "visual_home_clear_with_sections");

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();

        assertTrue(parity.matches());
        assertEquals(0L, result.diagnostics().timing().projectNanos());
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertEquals(SlotWorkspaceAtlasLayout.ISLAND_TRIAGE, card(result.viewModel(), "minecraft:stone").islandId());
        assertEquals("materials", card(result.viewModel(), "minecraft:dirt").islandId());
        assertEquals(1, result.viewModel().island("materials").carriedCount());
    }

    @Test
    void visualHomeReorderInvalidationSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity stone = new ItemIdentity("minecraft:stone", ItemComparisonMode.ITEM_ID, "");
        ItemIdentity dirt = new ItemIdentity("minecraft:dirt", ItemComparisonMode.ITEM_ID, "");
        WorkspaceProjectionRequest seed = request(authority(List.of(
                stack(0, "minecraft:stone", 16),
                stack(1, "minecraft:dirt", 4))), workflowWithHomes(homeMap(stone, dirt)), "ready", "", 0, 0, "");
        WorkspaceProjectionRequest changed = request(authority(List.of(
                stack(0, "minecraft:stone", 16),
                stack(1, "minecraft:dirt", 4))), workflowWithHomes(homeMap(dirt, stone)), "ready", "", 0, 0, "");

        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.COMMAND_OUTCOME,
                Set.of(stone, dirt),
                Set.of(),
                Set.of(),
                java.util.EnumSet.of(WorkspaceProjectionSlice.CARD, WorkspaceProjectionSlice.SECTION),
                false,
                "visual_home_reorder");
        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();

        assertTrue(parity.matches());
        assertEquals(0L, result.diagnostics().timing().projectNanos());
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertEquals("minecraft:dirt", result.viewModel().atlasItems().get(0).identity().itemId());
        assertEquals("minecraft:stone", result.viewModel().atlasItems().get(1).identity().itemId());
    }

    @Test
    void sectionMetadataInvalidationUpdatesIslandDescriptorSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity stone = new ItemIdentity("minecraft:stone", ItemComparisonMode.ITEM_ID, "");
        WorkspaceProjectionRequest seed = request(authority(List.of(
                stack(0, "minecraft:stone", 16))),
                workflowWithHomes(homeMapWithIsland("materials", "Materials", 0xCC334455, 100, 200, stone)),
                "ready",
                "",
                0,
                0,
                "");
        WorkspaceProjectionRequest changed = request(authority(List.of(
                stack(0, "minecraft:stone", 16))),
                workflowWithHomes(homeMapWithIsland("materials", "Blocks", 0xCC556677, -50, 80, stone)),
                "ready",
                "",
                0,
                0,
                "");
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.COMMAND_OUTCOME,
                Set.of(),
                Set.of(),
                Set.of("materials"),
                java.util.EnumSet.of(WorkspaceProjectionSlice.SECTION, WorkspaceProjectionSlice.FRAME),
                false,
                "island_metadata_changed");

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();
        SlotWorkspaceViewModel.AtlasIsland island = result.viewModel().island("materials");

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertEquals("Blocks", island.label());
        assertEquals(0xCC556677, island.color());
        assertEquals(-50, island.x());
        assertEquals(80, island.y());
        assertEquals(1, island.carriedCount());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void sectionMetadataInvalidationReordersIslandsSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity stone = new ItemIdentity("minecraft:stone", ItemComparisonMode.ITEM_ID, "");
        ItemIdentity dirt = new ItemIdentity("minecraft:dirt", ItemComparisonMode.ITEM_ID, "");
        WorkspaceProjectionRequest seed = request(authority(List.of(
                stack(0, "minecraft:stone", 16),
                stack(1, "minecraft:dirt", 4))),
                workflowWithHomes(twoIslandHomeMap(false, stone, dirt)),
                "ready",
                "",
                0,
                0,
                "");
        WorkspaceProjectionRequest changed = request(authority(List.of(
                stack(0, "minecraft:stone", 16),
                stack(1, "minecraft:dirt", 4))),
                workflowWithHomes(twoIslandHomeMap(true, stone, dirt)),
                "ready",
                "",
                0,
                0,
                "");
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.COMMAND_OUTCOME,
                Set.of(),
                Set.of(),
                Set.of("tools"),
                java.util.EnumSet.of(WorkspaceProjectionSlice.SECTION, WorkspaceProjectionSlice.FRAME),
                false,
                "island_reordered");

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertEquals("tools", result.viewModel().islands().get(0).islandId());
        assertEquals("materials", result.viewModel().islands().get(1).islandId());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void islandCreateInvalidationAddsSectionAndRehomesIdentitySkipsFullProjectionAndMatchesOracle() {
        ItemIdentity stone = new ItemIdentity("minecraft:stone", ItemComparisonMode.ITEM_ID, "");
        WorkspaceProjectionRequest seed = request(authority(List.of(
                stack(0, "minecraft:stone", 16))),
                workflowWithHomes(VisualHomeMap.empty()),
                "ready",
                "",
                0,
                0,
                "");
        WorkspaceProjectionRequest changed = request(authority(List.of(
                stack(0, "minecraft:stone", 16))),
                workflowWithHomes(homeMapWithIsland("ores", "Ores", 0xCC667788, 40, 60, stone)),
                "ready",
                "",
                0,
                0,
                "");
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.COMMAND_OUTCOME,
                Set.of(stone),
                Set.of(),
                Set.of(SlotWorkspaceAtlasLayout.ISLAND_TRIAGE, "ores"),
                java.util.EnumSet.of(WorkspaceProjectionSlice.CARD, WorkspaceProjectionSlice.SECTION),
                false,
                "island_created");

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertEquals("ores", card(result.viewModel(), "minecraft:stone").islandId());
        assertEquals("Ores", result.viewModel().island("ores").label());
        assertEquals(1, result.viewModel().island("ores").carriedCount());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void islandDeleteInvalidationClearsHomesAndRemovesSectionSkipsFullProjectionAndMatchesOracle() {
        ItemIdentity stone = new ItemIdentity("minecraft:stone", ItemComparisonMode.ITEM_ID, "");
        WorkspaceProjectionRequest seed = request(authority(List.of(
                stack(0, "minecraft:stone", 16))),
                workflowWithHomes(homeMapWithIsland("ores", "Ores", 0xCC667788, 40, 60, stone)),
                "ready",
                "",
                0,
                0,
                "");
        WorkspaceProjectionRequest changed = request(authority(List.of(
                stack(0, "minecraft:stone", 16))),
                workflowWithHomes(VisualHomeMap.empty()),
                "ready",
                "",
                0,
                0,
                "");
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.COMMAND_OUTCOME,
                Set.of(stone),
                Set.of(),
                Set.of("ores", SlotWorkspaceAtlasLayout.ISLAND_TRIAGE),
                java.util.EnumSet.of(WorkspaceProjectionSlice.CARD, WorkspaceProjectionSlice.SECTION, WorkspaceProjectionSlice.FRAME),
                false,
                "island_deleted");

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertEquals(SlotWorkspaceAtlasLayout.ISLAND_TRIAGE, card(result.viewModel(), "minecraft:stone").islandId());
        assertNull(result.viewModel().island("ores"));
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void emptyIslandDeleteInvalidationRemovesSectionSkipsFullProjectionAndMatchesOracle() {
        WorkspaceProjectionRequest seed = request(authority(List.of(
                stack(0, "minecraft:stone", 16))),
                workflowWithHomes(homeMapWithIsland("ores", "Ores", 0xCC667788, 40, 60)),
                "ready",
                "",
                0,
                0,
                "");
        WorkspaceProjectionRequest changed = request(authority(List.of(
                stack(0, "minecraft:stone", 16))),
                workflowWithHomes(VisualHomeMap.empty()),
                "ready",
                "",
                0,
                0,
                "");
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.COMMAND_OUTCOME,
                Set.of(),
                Set.of(),
                Set.of("ores"),
                java.util.EnumSet.of(WorkspaceProjectionSlice.SECTION, WorkspaceProjectionSlice.FRAME),
                false,
                "island_deleted");

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertNull(result.viewModel().island("ores"));
        assertEquals(SlotWorkspaceAtlasLayout.ISLAND_TRIAGE, card(result.viewModel(), "minecraft:stone").islandId());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void searchQueryPromotesIntentAndLocalizesHomedTrackedStorageGhosts() {
        ItemIdentity redstone = new ItemIdentity("minecraft:redstone", ItemComparisonMode.ITEM_ID, "");
        WorkspaceStorageIndex storageIndex = storageIndex(List.of(
                remoteDisplayStorageEntry("storage-a", 1, "minecraft:redstone", 8),
                remoteDisplayStorageEntry("storage-b", 2, "minecraft:dirt", 5)), 1L);
        WorkspaceProjectionRequest seed = storageRequestWithRemoteIntent(
                InventoryAuthoritySnapshot.empty(),
                workflowWithHomes(homeMap(redstone)),
                storageIndex,
                RemoteStorageDetailIntent.INTENT_ONLY);
        WorkspaceProjectionRequest changed = storageRequestWithRemoteIntent(
                InventoryAuthoritySnapshot.empty(),
                workflowWithHomes(homeMap(redstone)),
                storageIndex,
                "redstone",
                RemoteStorageDetailIntent.INTENT_ONLY);
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.SEARCH_QUERY_CHANGED,
                Set.of(),
                Set.of(),
                Set.of(),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.FRAME),
                false,
                "remote_search_query_changed");

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos());
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertEquals(8, card(result.viewModel(), "minecraft:redstone").totalCount());
        assertEquals("materials", card(result.viewModel(), "minecraft:redstone").islandId());
        assertFalse(hasCard(result.viewModel(), "minecraft:dirt"));
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void explicitRemoteSearchQueryChangeUsesRequestScopedTrackedDisplayEntries() {
        ItemIdentity dirt = new ItemIdentity("minecraft:dirt", ItemComparisonMode.ITEM_ID, "");
        WorkspaceStorageIndex storageIndex = storageIndex(List.of(
                remoteDisplayStorageEntry("storage-a", 1, "minecraft:redstone", 8),
                rememberedRemoteDisplayStorageEntry("storage-b", 2, "minecraft:dirt", 5)), 1L);
        WorkspaceProjectionRequest seed = storageRequestWithRemoteIntent(
                InventoryAuthoritySnapshot.empty(),
                workflowWithHomes(homeMap(dirt)),
                storageIndex,
                RemoteStorageDetailIntent.INTENT_ONLY);
        WorkspaceProjectionRequest changed = storageRequestWithRemoteIntent(
                InventoryAuthoritySnapshot.empty(),
                workflowWithHomes(homeMap(dirt)),
                storageIndex,
                "dirt",
                RemoteStorageDetailIntent.SEARCH);
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.SEARCH_QUERY_CHANGED,
                Set.of(),
                Set.of(),
                Set.of(),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.FRAME),
                false,
                "remote_search_query_changed_scoped_to_request");

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos());
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertFalse(hasCard(result.viewModel(), "minecraft:dirt"));
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void remoteDetailSearchIntentChangeLocalizesHomedTrackedStorageGhosts() {
        ItemIdentity redstone = new ItemIdentity("minecraft:redstone", ItemComparisonMode.ITEM_ID, "");
        WorkspaceStorageIndex storageIndex = storageIndex(List.of(
                remoteDisplayStorageEntry("storage-a", 1, "minecraft:redstone", 8)), 1L);
        WorkspaceProjectionRequest seed = storageRequestWithRemoteIntent(
                InventoryAuthoritySnapshot.empty(),
                workflowWithHomes(homeMap(redstone)),
                storageIndex,
                "redstone",
                RemoteStorageDetailIntent.INTENT_ONLY);
        WorkspaceProjectionRequest changed = storageRequestWithRemoteIntent(
                InventoryAuthoritySnapshot.empty(),
                workflowWithHomes(homeMap(redstone)),
                storageIndex,
                "redstone",
                RemoteStorageDetailIntent.SEARCH);
        WorkspaceInvalidation invalidation = remoteDetailInvalidation("remote_detail_search_enabled");

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertEquals(8, card(result.viewModel(), "minecraft:redstone").totalCount());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void remoteDetailLeavingSearchKeepsQueryMatchedRemoteGhostsWithoutFullProjection() {
        ItemIdentity redstone = new ItemIdentity("minecraft:redstone", ItemComparisonMode.ITEM_ID, "");
        WorkspaceStorageIndex storageIndex = storageIndex(List.of(
                remoteDisplayStorageEntry("storage-a", 1, "minecraft:redstone", 8)), 1L);
        WorkspaceProjectionRequest seed = storageRequestWithRemoteIntent(
                InventoryAuthoritySnapshot.empty(),
                workflowWithHomes(homeMap(redstone)),
                storageIndex,
                "redstone",
                RemoteStorageDetailIntent.SEARCH);
        WorkspaceProjectionRequest changed = storageRequestWithRemoteIntent(
                InventoryAuthoritySnapshot.empty(),
                workflowWithHomes(homeMap(redstone)),
                storageIndex,
                "redstone",
                RemoteStorageDetailIntent.INTENT_ONLY);

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(
                        seed,
                        changed,
                        List.of(remoteDetailInvalidation("remote_detail_search_disabled")));
        WorkspaceProjectionResult result = parity.invalidated();

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertEquals(8, card(result.viewModel(), "minecraft:redstone").totalCount());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void remoteSearchQueryClearRemovesRemoteOnlyGhostsWithoutFullProjection() {
        ItemIdentity redstone = new ItemIdentity("minecraft:redstone", ItemComparisonMode.ITEM_ID, "");
        WorkspaceStorageIndex storageIndex = storageIndex(List.of(
                remoteDisplayStorageEntry("storage-a", 1, "minecraft:redstone", 8)), 1L);
        WorkspaceProjectionRequest seed = storageRequestWithRemoteIntent(
                InventoryAuthoritySnapshot.empty(),
                workflowWithHomes(homeMap(redstone)),
                storageIndex,
                "redstone",
                RemoteStorageDetailIntent.SEARCH);
        WorkspaceProjectionRequest changed = storageRequestWithRemoteIntent(
                InventoryAuthoritySnapshot.empty(),
                workflowWithHomes(homeMap(redstone)),
                storageIndex,
                "",
                RemoteStorageDetailIntent.SEARCH);
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.SEARCH_QUERY_CHANGED,
                Set.of(),
                Set.of(),
                Set.of(),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.FRAME),
                false,
                "remote_search_query_cleared");

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos(), projectionDebug(result));
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertFalse(hasCard(result.viewModel(), "minecraft:redstone"));
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void recipeRemoteDetailIdentitiesMaterializeStoredOnlyIngredientForSidebar() {
        ItemIdentity brick = new ItemIdentity("minecraft:brick", ItemComparisonMode.ITEM_ID, "");
        WorkspaceStorageIndex storageIndex = storageIndex(List.of(
                remoteDisplayStorageEntry("storage-a", 1, "minecraft:brick", 4)), 1L);
        WorkspaceProjectionRequest request = storageRequestWithRemoteDetailIdentities(
                InventoryAuthoritySnapshot.empty(),
                WorkflowDomainSnapshot.empty(),
                storageIndex,
                Set.of(brick));

        WorkspaceProjectionResult result = new WorkspaceProjectionSessionCache().project(
                request,
                List.of(WorkspaceInvalidation.full(
                        WorkspaceInvalidation.Reason.SESSION_OPEN,
                        "recipe_remote_detail_test")));

        SlotWorkspaceViewModel.AtlasItem sourceCard = card(result.viewModel(), "minecraft:brick");
        assertTrue(sourceCard.ghost());
        assertFalse(sourceCard.carried());
        assertEquals(4, sourceCard.totalCount());
        assertEquals(1, sourceCard.elsewhere().size());

        RecipeIngredientSidebarSpec sidebarSpec = new RecipeIngredientSidebarSpec(
                "emi:test",
                "Recipe ingredients",
                List.of(new RecipeIngredientSidebarSpec.Ingredient(
                        "brick",
                        "Brick",
                        4,
                        List.of(new RecipeIngredientSidebarSpec.Alternative(
                                brick,
                                "Brick",
                                4,
                                new ItemStack("minecraft:brick", 4, 64))))));
        RecipeIngredientSidebarSpec.Projection sidebar = sidebarSpec.project(result.viewModel());
        SlotWorkspaceViewModel.AtlasItem sidebarCard =
                sidebar.atlasItem(SlotWorkspaceViewModel.IdentityRef.from(brick));

        assertEquals(4, sidebarCard.totalCount());
        assertEquals(4, sidebarCard.desiredCount());
        assertFalse(sidebarCard.carried());
        assertEquals(4, sidebar.ingredient(sidebarCard).requiredCount());
    }

    @Test
    void activeChestPanelInvalidationSkipsFullProjectionAndMatchesOracle() {
        WorkspaceProjectionRequest seed = request(authority("minecraft:stone", 16), "ready", "", 0, 0, "");
        SlotWorkspaceViewModel.ActiveChestPanel activePanel = new SlotWorkspaceViewModel.ActiveChestPanel(
                "storage-a",
                "Ore Chest",
                "cluster-a",
                "Mine",
                0xFF336699,
                12,
                64,
                -5,
                "minecraft:overworld",
                ChestRole.STORAGE,
                List.of());
        WorkspaceProjectionRequest changed = requestWithActiveChestPanel(
                authority("minecraft:stone", 16),
                activePanel);
        WorkspaceInvalidation invalidation = new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.PROXIMITY_CHANGED,
                Set.of(),
                Set.of(),
                Set.of(),
                java.util.EnumSet.of(WorkspaceProjectionSlice.PANEL, WorkspaceProjectionSlice.FRAME),
                false,
                "active_chest_panel_changed");

        WorkspaceProjectionParityHarness.Result parity =
                new WorkspaceProjectionParityHarness().compareAfterSeed(seed, changed, List.of(invalidation));
        WorkspaceProjectionResult result = parity.invalidated();

        assertTrue(parity.matches(), parityDebug(parity));
        assertEquals(0L, result.diagnostics().timing().projectNanos());
        assertEquals("", result.diagnostics().fullProjectionReason());
        assertEquals(activePanel, result.viewModel().activeChestPanel());
        assertTrue(result.diagnostics().projectionFactsReused() > 0);
    }

    @Test
    void parityHarnessComparesInvalidatedProjectionAgainstFreshFullOracle() {
        WorkspaceProjectionRequest seed = request(authority("minecraft:stone", 16), "ready", "", 0, 0, "");
        WorkspaceProjectionRequest changed = request(authority("minecraft:stone", 17), "ready", "", 0, 0, "");
        WorkspaceProjectionParityHarness.Result result = new WorkspaceProjectionParityHarness().compareAfterSeed(
                seed,
                changed,
                List.of(WorkspaceInvalidation.full(
                        WorkspaceInvalidation.Reason.CARRIED_REVISION_CHANGED,
                        "synthetic_carried_change")));

        assertTrue(result.matches());
        assertEquals(result.full().contentFingerprint(), result.invalidated().contentFingerprint());
    }

    @Test
    void projectionStoreMaterializesSourceAndStorageFacts() {
        WorkspaceStorageIndex storageIndex = storageIndex("minecraft:stone", "{quality=stored}", 4);
        WorkspaceProjectionStore store = WorkspaceProjectionStore.from(new WorkspaceProjectionRequest(
                authority("minecraft:stone", "{quality=carried}", 16),
                WorkflowDomainSnapshot.empty(),
                "ready",
                "",
                0,
                0,
                0,
                null,
                null,
                null,
                Set.of(),
                null,
                null,
                "",
                null,
                0L,
                SlotWorkspaceViewModel.ActiveChestPanel.empty(),
                List.of(),
                Set.of(),
                List.of(),
                storageIndex.liveTrackedDisplayEntries(),
                storageIndex.liveDepositStorageIds(),
                storageIndex,
                null,
                null));

        assertEquals(1, store.sourceEntries().size());
        WorkspaceProjectionStore.SourceEntryFact source = store.sourceEntries().values().iterator().next();
        assertEquals("minecraft:stone", source.exactStackKey().itemId());
        assertEquals("{quality=carried}", source.exactStackKey().dataFingerprint());
        assertEquals(16, store.carriedIdentities().values().iterator().next().totalCount());
        assertEquals(1, store.storageMeta().size());
        assertEquals(1, store.storageContents().size());
        assertEquals(1, store.storagePresence().size());
        WorkspaceProjectionStore.StoragePresenceFact presence = store.storagePresence().values().iterator().next();
        assertEquals(4, presence.count());
        assertEquals("{quality=stored}", WorkspaceProjectionStore.ExactStackKey.from(
                presence.representativeDisplayStack()).dataFingerprint());
    }

    @Test
    void projectionStoreUpdatesAffectedCarriedIdentityFactsOnly() {
        ItemIdentity stone = new ItemIdentity("minecraft:stone", ItemComparisonMode.ITEM_ID, "");
        WorkspaceProjectionStore seed = WorkspaceProjectionStore.from(request(authority(List.of(
                stack(0, "minecraft:stone", 16),
                stack(1, "minecraft:dirt", 4))), "ready", "", 0, 0, ""));
        WorkspaceProjectionRequest changed = request(authority(List.of(
                stack(0, "minecraft:stone", 17),
                stack(1, "minecraft:dirt", 4))), "ready", "", 0, 0, "");

        WorkspaceProjectionStore.UpdateResult update = seed.updateFrom(
                changed,
                WorkspaceInvalidationSummary.coalesce(List.of(WorkspaceInvalidation.localizedIdentity(
                        WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED,
                        stone,
                        java.util.EnumSet.of(WorkspaceProjectionSlice.CARD, WorkspaceProjectionSlice.SECTION),
                        "test_identity_fact_update"))));

        assertTrue(update.localized());
        assertTrue(update.factsUpdated() > 0);
        assertTrue(update.factsReused() > 0);
        assertEquals(2, update.store().carriedIdentities().size());
        assertEquals(17, carriedFact(update.store(), "minecraft:stone").totalCount());
        assertEquals(4, carriedFact(update.store(), "minecraft:dirt").totalCount());
        assertEquals(2, update.store().sourceEntries().size());
    }

    @Test
    void projectionStoreUpdatesAffectedStorageFactsOnly() {
        WorkspaceProjectionStore seed = WorkspaceProjectionStore.from(storageRequest(storageIndex(List.of(
                displayStorageEntry("storage-a", 1, "minecraft:stone", 4),
                displayStorageEntry("storage-b", 2, "minecraft:dirt", 9)), 1L)));
        WorkspaceProjectionRequest changed = storageRequest(storageIndex(List.of(
                displayStorageEntry("storage-a", 1, "minecraft:stone", 5),
                displayStorageEntry("storage-b", 2, "minecraft:dirt", 9)), 2L));

        WorkspaceProjectionStore.UpdateResult update = seed.updateFrom(
                changed,
                WorkspaceInvalidationSummary.coalesce(List.of(WorkspaceInvalidation.localizedStorage(
                        WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED,
                        "storage-a",
                        java.util.EnumSet.of(WorkspaceProjectionSlice.STORAGE, WorkspaceProjectionSlice.WAYFINDING),
                        "test_storage_fact_update"))));

        assertTrue(update.localized());
        assertTrue(update.factsUpdated() > 0);
        assertTrue(update.factsReused() > 0);
        assertEquals(2, update.store().storageMeta().size());
        assertEquals(2, update.store().storageContents().size());
        assertEquals(2, update.store().storagePresence().size());
        assertEquals(5, storagePresence(update.store(), "storage-a", "minecraft:stone").count());
        assertEquals(9, storagePresence(update.store(), "storage-b", "minecraft:dirt").count());
    }

    private static WorkspaceProjectionRequest request(
            InventoryAuthoritySnapshot authority,
            String status,
            String diagnostics,
            int pendingCount,
            int selectedSlot,
            String searchQuery
    ) {
        return request(
                authority,
                status,
                diagnostics,
                pendingCount,
                selectedSlot,
                searchQuery,
                RemoteStorageDetailIntent.INTENT_ONLY);
    }

    private static WorkspaceProjectionRequest requestWithContainerResolver(
            InventoryAuthoritySnapshot authority,
            java.util.function.Function<ItemIdentity, SlotWorkspaceViewModel.CarriedContainerInfo> resolver
    ) {
        return new WorkspaceProjectionRequest(
                authority,
                WorkflowDomainSnapshot.empty(),
                "ready",
                "",
                0,
                0,
                0,
                null,
                null,
                null,
                Set.of(),
                resolver,
                null,
                "",
                RemoteStorageDetailIntent.INTENT_ONLY,
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

    private static WorkspaceProjectionRequest requestWithActiveChestPanel(
            InventoryAuthoritySnapshot authority,
            SlotWorkspaceViewModel.ActiveChestPanel activeChestPanel
    ) {
        return new WorkspaceProjectionRequest(
                authority,
                WorkflowDomainSnapshot.empty(),
                "ready",
                "",
                0,
                0,
                0,
                null,
                null,
                null,
                Set.of(),
                null,
                null,
                "",
                RemoteStorageDetailIntent.INTENT_ONLY,
                0L,
                activeChestPanel,
                List.of(),
                Set.of(),
                List.of(),
                List.of(),
                Set.of(),
                WorkspaceStorageIndex.empty(),
                null,
                null);
    }

    private static java.util.function.Function<ItemIdentity, SlotWorkspaceViewModel.CarriedContainerInfo> containerResolver(
            ItemIdentity container,
            int freeSlots,
            int slotCapacity
    ) {
        return identity -> identity != null
                && container != null
                && identity.itemId().equals(container.itemId())
                ? new SlotWorkspaceViewModel.CarriedContainerInfo(freeSlots, slotCapacity)
                : null;
    }

    private static WorkspaceProjectionRequest request(
            InventoryAuthoritySnapshot authority,
            WorkflowDomainSnapshot workflow,
            String status,
            String diagnostics,
            int pendingCount,
            int selectedSlot,
            String searchQuery
    ) {
        return request(
                authority,
                workflow,
                status,
                diagnostics,
                pendingCount,
                selectedSlot,
                searchQuery,
                RemoteStorageDetailIntent.INTENT_ONLY);
    }

    private static WorkspaceProjectionRequest request(
            InventoryAuthoritySnapshot authority,
            String status,
            String diagnostics,
            int pendingCount,
            int selectedSlot,
            String searchQuery,
            RemoteStorageDetailIntent remoteStorageDetailIntent
    ) {
        return request(
                authority,
                WorkflowDomainSnapshot.empty(),
                status,
                diagnostics,
                pendingCount,
                selectedSlot,
                searchQuery,
                remoteStorageDetailIntent);
    }

    private static WorkspaceProjectionRequest request(
            InventoryAuthoritySnapshot authority,
            WorkflowDomainSnapshot workflow,
            String status,
            String diagnostics,
            int pendingCount,
            int selectedSlot,
            String searchQuery,
            RemoteStorageDetailIntent remoteStorageDetailIntent
    ) {
        return new WorkspaceProjectionRequest(
                authority,
                workflow,
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
                remoteStorageDetailIntent,
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

    private static WorkflowDomainSnapshot workflowWithHomes(VisualHomeMap visualHomeMap) {
        return workflowWithHomesAndTargets(visualHomeMap, Map.of(), Map.of());
    }

    private static WorkflowDomainSnapshot workflowWithHomesAndTargets(
            VisualHomeMap visualHomeMap,
            Map<ItemIdentity, Integer> playerDesiredCounts,
            Map<ItemIdentity, Integer> playerWantedCounts
    ) {
        return workflowWithHomesAndTargetsAndChests(
                visualHomeMap,
                playerDesiredCounts,
                playerWantedCounts,
                null);
    }

    private static WorkflowDomainSnapshot workflowWithHomesAndCraftRun(
            VisualHomeMap visualHomeMap,
            CraftRunState craftRun
    ) {
        WorkflowDomainSnapshot base = workflowWithHomes(visualHomeMap);
        return new WorkflowDomainSnapshot(
                base.nextGlobalSequence(),
                base.workflowProjection(),
                base.workflowEvents(),
                base.activityProjection(),
                base.activityEvents(),
                base.browsePreferences(),
                base.browseSessionState(),
                craftRun,
                base.contextualSuggestions());
    }

    private static WorkflowDomainSnapshot workflowWithHomesCraftRunAndChests(
            VisualHomeMap visualHomeMap,
            CraftRunState craftRun,
            ClaimedChestMap claimedChestMap
    ) {
        WorkflowDomainSnapshot base = workflowWithHomesAndTargetsAndChests(
                visualHomeMap,
                Map.of(),
                Map.of(),
                claimedChestMap);
        return new WorkflowDomainSnapshot(
                base.nextGlobalSequence(),
                base.workflowProjection(),
                base.workflowEvents(),
                base.activityProjection(),
                base.activityEvents(),
                base.browsePreferences(),
                base.browseSessionState(),
                craftRun,
                base.contextualSuggestions());
    }

    private static WorkflowDomainSnapshot workflowWithKitMap(
            VisualHomeMap visualHomeMap,
            KitMap kitMap
    ) {
        return workflowWithKitMapAndChests(visualHomeMap, kitMap, null);
    }

    private static WorkflowDomainSnapshot workflowWithKitMapAndChests(
            VisualHomeMap visualHomeMap,
            KitMap kitMap,
            ClaimedChestMap claimedChestMap
    ) {
        return workflowWithKitMapCountsAndChests(visualHomeMap, kitMap, Map.of(), Map.of(), claimedChestMap);
    }

    private static WorkflowDomainSnapshot workflowWithKitMapChestsAndAffinity(
            VisualHomeMap visualHomeMap,
            KitMap kitMap,
            ClaimedChestMap claimedChestMap,
            ChestAffinityMap affinityMap
    ) {
        WorkflowDomainSnapshot empty = WorkflowDomainSnapshot.empty();
        WorkflowProjection.Snapshot base = empty.workflowProjection();
        WorkflowProjection.Snapshot projection = new WorkflowProjection.Snapshot(
                base.userCollections(),
                base.memberships(),
                base.loadoutsByCollection(),
                base.favoriteTags(),
                base.junkTags(),
                base.junkMarkedAtEpochMillis(),
                base.protection(),
                base.recentDismissedUpToByIdentity(),
                visualHomeMap == null ? base.visualHomeMap() : visualHomeMap,
                claimedChestMap == null ? base.claimedChestMap() : claimedChestMap,
                affinityMap == null ? base.chestAffinityMap() : affinityMap,
                base.clusterLabels(),
                kitMap == null ? base.kitMap() : kitMap,
                base.playerDesiredCounts(),
                base.kitDesiredCounts(),
                base.playerWantedCounts(),
                base.kitWantedCounts());
        return new WorkflowDomainSnapshot(
                empty.nextGlobalSequence(),
                projection,
                empty.workflowEvents(),
                empty.activityProjection(),
                empty.activityEvents(),
                empty.browsePreferences(),
                empty.browseSessionState(),
                empty.craftRun(),
                empty.contextualSuggestions());
    }

    private static WorkflowDomainSnapshot workflowWithKitMapCountsAndChests(
            VisualHomeMap visualHomeMap,
            KitMap kitMap,
            Map<String, Map<ItemIdentity, Integer>> kitDesiredCounts,
            Map<String, Map<ItemIdentity, Integer>> kitWantedCounts,
            ClaimedChestMap claimedChestMap
    ) {
        WorkflowDomainSnapshot empty = WorkflowDomainSnapshot.empty();
        WorkflowProjection.Snapshot base = empty.workflowProjection();
        WorkflowProjection.Snapshot projection = new WorkflowProjection.Snapshot(
                base.userCollections(),
                base.memberships(),
                base.loadoutsByCollection(),
                base.favoriteTags(),
                base.junkTags(),
                base.junkMarkedAtEpochMillis(),
                base.protection(),
                base.recentDismissedUpToByIdentity(),
                visualHomeMap == null ? base.visualHomeMap() : visualHomeMap,
                claimedChestMap == null ? base.claimedChestMap() : claimedChestMap,
                base.chestAffinityMap(),
                base.clusterLabels(),
                kitMap == null ? base.kitMap() : kitMap,
                base.playerDesiredCounts(),
                kitDesiredCounts == null ? base.kitDesiredCounts() : kitDesiredCounts,
                base.playerWantedCounts(),
                kitWantedCounts == null ? base.kitWantedCounts() : kitWantedCounts);
        return new WorkflowDomainSnapshot(
                empty.nextGlobalSequence(),
                projection,
                empty.workflowEvents(),
                empty.activityProjection(),
                empty.activityEvents(),
                empty.browsePreferences(),
                empty.browseSessionState(),
                empty.craftRun(),
                empty.contextualSuggestions());
    }

    private static WorkflowDomainSnapshot workflowWithHomesAndTargetsAndChests(
            VisualHomeMap visualHomeMap,
            Map<ItemIdentity, Integer> playerDesiredCounts,
            Map<ItemIdentity, Integer> playerWantedCounts,
            ClaimedChestMap claimedChestMap
    ) {
        return workflowWithHomesTargetsChestsAndAffinity(
                visualHomeMap,
                playerDesiredCounts,
                playerWantedCounts,
                claimedChestMap,
                ChestAffinityMap.empty());
    }

    private static WorkflowDomainSnapshot workflowWithHomesTargetsChestsAndAffinity(
            VisualHomeMap visualHomeMap,
            Map<ItemIdentity, Integer> playerDesiredCounts,
            Map<ItemIdentity, Integer> playerWantedCounts,
            ClaimedChestMap claimedChestMap,
            ChestAffinityMap affinityMap
    ) {
        return workflowWithHomesTargetsChestsAffinityAndClusterLabels(
                visualHomeMap,
                playerDesiredCounts,
                playerWantedCounts,
                claimedChestMap,
                affinityMap,
                Map.of());
    }

    private static WorkflowDomainSnapshot workflowWithHomesTargetsChestsAffinityAndClusterLabels(
            VisualHomeMap visualHomeMap,
            Map<ItemIdentity, Integer> playerDesiredCounts,
            Map<ItemIdentity, Integer> playerWantedCounts,
            ClaimedChestMap claimedChestMap,
            ChestAffinityMap affinityMap,
            Map<String, String> clusterLabels
    ) {
        WorkflowDomainSnapshot empty = WorkflowDomainSnapshot.empty();
        WorkflowProjection.Snapshot base = empty.workflowProjection();
        WorkflowProjection.Snapshot projection = new WorkflowProjection.Snapshot(
                base.userCollections(),
                base.memberships(),
                base.loadoutsByCollection(),
                base.favoriteTags(),
                base.junkTags(),
                base.junkMarkedAtEpochMillis(),
                base.protection(),
                base.recentDismissedUpToByIdentity(),
                visualHomeMap,
                claimedChestMap == null ? base.claimedChestMap() : claimedChestMap,
                affinityMap == null ? base.chestAffinityMap() : affinityMap,
                clusterLabels == null ? base.clusterLabels() : clusterLabels,
                base.kitMap(),
                playerDesiredCounts == null ? base.playerDesiredCounts() : playerDesiredCounts,
                base.kitDesiredCounts(),
                playerWantedCounts == null ? base.playerWantedCounts() : playerWantedCounts,
                base.kitWantedCounts());
        return new WorkflowDomainSnapshot(
                empty.nextGlobalSequence(),
                projection,
                empty.workflowEvents(),
                empty.activityProjection(),
                empty.activityEvents(),
                empty.browsePreferences(),
                empty.browseSessionState(),
                empty.craftRun(),
                empty.contextualSuggestions());
    }

    private static WorkflowDomainSnapshot workflowWithJunk(
            VisualHomeMap visualHomeMap,
            Set<ItemIdentity> junkTags
    ) {
        WorkflowDomainSnapshot empty = WorkflowDomainSnapshot.empty();
        WorkflowProjection.Snapshot base = empty.workflowProjection();
        java.util.LinkedHashMap<ItemIdentity, Long> markedAt = new java.util.LinkedHashMap<>();
        if (junkTags != null) {
            for (ItemIdentity identity : junkTags) {
                if (identity != null) {
                    markedAt.put(identity, 1L);
                }
            }
        }
        WorkflowProjection.Snapshot projection = new WorkflowProjection.Snapshot(
                base.userCollections(),
                base.memberships(),
                base.loadoutsByCollection(),
                base.favoriteTags(),
                junkTags == null ? base.junkTags() : junkTags,
                markedAt.isEmpty() ? base.junkMarkedAtEpochMillis() : markedAt,
                base.protection(),
                base.recentDismissedUpToByIdentity(),
                visualHomeMap,
                base.claimedChestMap(),
                base.chestAffinityMap(),
                base.clusterLabels(),
                base.kitMap(),
                base.playerDesiredCounts(),
                base.kitDesiredCounts(),
                base.playerWantedCounts(),
                base.kitWantedCounts());
        return new WorkflowDomainSnapshot(
                empty.nextGlobalSequence(),
                projection,
                empty.workflowEvents(),
                empty.activityProjection(),
                empty.activityEvents(),
                empty.browsePreferences(),
                empty.browseSessionState(),
                empty.craftRun(),
                empty.contextualSuggestions());
    }

    private static CraftRunState craftRun(ItemIdentity input, int requiredCountPerBatch, int remainingOutputCount) {
        ItemIdentity output = new ItemIdentity("minecraft:comparator", ItemComparisonMode.ITEM_ID, "");
        CraftRunRecipeEntry entry = new CraftRunRecipeEntry(
                "craft-run-1",
                1L,
                "test",
                "minecraft:comparator",
                "Comparator",
                output,
                "Comparator",
                1,
                remainingOutputCount,
                List.of(new CraftRunIngredientGroup(
                        "ingredient-1",
                        input == null ? "Ingredient" : input.itemId(),
                        requiredCountPerBatch,
                        true,
                        List.of(new CraftRunAlternative(input, input == null ? "Ingredient" : input.itemId())),
                        List.of())),
                List.of());
        return new CraftRunState(1, entry.entryId(), List.of(entry));
    }

    private static CraftRunState craftRunWithSelectedAlternative(
            ItemIdentity selected,
            ItemIdentity other,
            int requiredCountPerBatch,
            int remainingOutputCount
    ) {
        ItemIdentity output = new ItemIdentity("minecraft:comparator", ItemComparisonMode.ITEM_ID, "");
        CraftRunRecipeEntry entry = new CraftRunRecipeEntry(
                "craft-run-1",
                1L,
                "test",
                "minecraft:comparator",
                "Comparator",
                output,
                "Comparator",
                1,
                remainingOutputCount,
                List.of(new CraftRunIngredientGroup(
                        "ingredient-1",
                        "Comparator dust",
                        requiredCountPerBatch,
                        true,
                        selected,
                        List.of(
                                new CraftRunAlternative(selected, selected == null ? "Selected" : selected.itemId()),
                                new CraftRunAlternative(other, other == null ? "Other" : other.itemId())),
                        List.of())),
                List.of());
        return new CraftRunState(1, entry.entryId(), List.of(entry));
    }

    private static CraftRunState craftRunWithAlternatives(
            ItemIdentity first,
            ItemIdentity second,
            int requiredCountPerBatch,
            int remainingOutputCount
    ) {
        ItemIdentity output = new ItemIdentity("minecraft:comparator", ItemComparisonMode.ITEM_ID, "");
        CraftRunRecipeEntry entry = new CraftRunRecipeEntry(
                "craft-run-1",
                1L,
                "test",
                "minecraft:comparator",
                "Comparator",
                output,
                "Comparator",
                1,
                remainingOutputCount,
                List.of(new CraftRunIngredientGroup(
                        "ingredient-1",
                        "Comparator dust",
                        requiredCountPerBatch,
                        true,
                        List.of(
                                new CraftRunAlternative(first, first == null ? "First" : first.itemId()),
                                new CraftRunAlternative(second, second == null ? "Second" : second.itemId())),
                        List.of())),
                List.of());
        return new CraftRunState(1, entry.entryId(), List.of(entry));
    }

    private static VisualHomeMap homeMap(ItemIdentity... identities) {
        return homeMapWithIsland("materials", "Materials", 0xCC334455, 100, 200, identities);
    }

    private static VisualHomeMap homeMapWithIsland(
            String islandId,
            String label,
            int color,
            double x,
            double y,
            ItemIdentity... identities
    ) {
        java.util.LinkedHashMap<ItemIdentity, VisualHomeAssignment> assignments = new java.util.LinkedHashMap<>();
        for (int i = 0; i < identities.length; i++) {
            ItemIdentity identity = identities[i];
            assignments.put(identity, new VisualHomeAssignment(
                    identity,
                    islandId,
                    i,
                    VisualHomeOrigin.PLAYER_PLACED,
                    false));
        }
        return new VisualHomeMap(
                List.of(new VisualAtlasIsland(
                        islandId,
                        label,
                        VisualAtlasIslandKind.PLAYER,
                        x,
                        y,
                        color,
                        null)),
                assignments,
                Set.of());
    }

    private static VisualHomeMap twoIslandHomeMap(boolean toolsFirst, ItemIdentity stone, ItemIdentity dirt) {
        VisualAtlasIsland materials = new VisualAtlasIsland(
                "materials",
                "Materials",
                VisualAtlasIslandKind.PLAYER,
                100,
                200,
                0xCC334455,
                null);
        VisualAtlasIsland tools = new VisualAtlasIsland(
                "tools",
                "Tools",
                VisualAtlasIslandKind.PLAYER,
                300,
                400,
                0xCC775533,
                null);
        java.util.LinkedHashMap<ItemIdentity, VisualHomeAssignment> assignments = new java.util.LinkedHashMap<>();
        assignments.put(stone, new VisualHomeAssignment(
                stone,
                "materials",
                0,
                VisualHomeOrigin.PLAYER_PLACED,
                false));
        assignments.put(dirt, new VisualHomeAssignment(
                dirt,
                "tools",
                0,
                VisualHomeOrigin.PLAYER_PLACED,
                false));
        return new VisualHomeMap(
                toolsFirst ? List.of(tools, materials) : List.of(materials, tools),
                assignments,
                Set.of());
    }

    private static WorkspaceProjectionRequest storageRequest(WorkspaceStorageIndex storageIndex) {
        return storageRequest(InventoryAuthoritySnapshot.empty(), WorkflowDomainSnapshot.empty(), storageIndex);
    }

    private static WorkspaceProjectionRequest storageRequest(
            InventoryAuthoritySnapshot authority,
            WorkflowDomainSnapshot workflow,
            WorkspaceStorageIndex storageIndex
    ) {
        return storageRequest(authority, workflow, storageIndex, false);
    }

    private static WorkspaceProjectionRequest storageRequestWithResolver(
            InventoryAuthoritySnapshot authority,
            WorkflowDomainSnapshot workflow,
            WorkspaceStorageIndex storageIndex
    ) {
        return storageRequest(authority, workflow, storageIndex, true);
    }

    private static WorkspaceInvalidation remoteDetailInvalidation(String diagnostics) {
        return new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.REMOTE_STORAGE_DETAIL_CHANGED,
                Set.of(),
                Set.of(),
                Set.of(),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.CARD,
                        WorkspaceProjectionSlice.SECTION,
                        WorkspaceProjectionSlice.FRAME),
                false,
                diagnostics);
    }

    private static WorkspaceInvalidation workflowMetadataInvalidation(String diagnostics) {
        return new WorkspaceInvalidation(
                WorkspaceInvalidation.Reason.WORKFLOW_METADATA_CHANGED,
                Set.of(),
                Set.of(),
                Set.of(),
                java.util.EnumSet.of(
                        WorkspaceProjectionSlice.WORKFLOW,
                        WorkspaceProjectionSlice.FRAME),
                false,
                diagnostics);
    }

    private static WorkspaceProjectionRequest storageRequestWithRemoteIntent(
            InventoryAuthoritySnapshot authority,
            WorkflowDomainSnapshot workflow,
            WorkspaceStorageIndex storageIndex,
            RemoteStorageDetailIntent remoteStorageDetailIntent
    ) {
        return storageRequestWithRemoteIntent(authority, workflow, storageIndex, "", remoteStorageDetailIntent);
    }

    private static WorkspaceProjectionRequest storageRequestWithRemoteIntent(
            InventoryAuthoritySnapshot authority,
            WorkflowDomainSnapshot workflow,
            WorkspaceStorageIndex storageIndex,
            String searchQuery,
            RemoteStorageDetailIntent remoteStorageDetailIntent
    ) {
        return storageRequest(
                authority,
                workflow,
                storageIndex,
                true,
                false,
                Set.of(),
                Set.of(),
                null,
                null,
                searchQuery,
                remoteStorageDetailIntent);
    }

    private static WorkspaceProjectionRequest storageRequestWithRemoteDetailIdentities(
            InventoryAuthoritySnapshot authority,
            WorkflowDomainSnapshot workflow,
            WorkspaceStorageIndex storageIndex,
            Set<ItemIdentity> remoteDetailIdentities
    ) {
        return storageRequest(
                authority,
                workflow,
                storageIndex,
                true,
                false,
                Set.of(),
                Set.of(),
                null,
                null,
                "",
                RemoteStorageDetailIntent.INTENT_ONLY,
                remoteDetailIdentities);
    }

    private static WorkspaceProjectionRequest storageRequestWithResolverAndDisplaySources(
            InventoryAuthoritySnapshot authority,
            WorkflowDomainSnapshot workflow,
            WorkspaceStorageIndex storageIndex
    ) {
        return storageRequest(authority, workflow, storageIndex, true, true);
    }

    private static WorkspaceProjectionRequest storageRequestWithResolverAndProximateIds(
            InventoryAuthoritySnapshot authority,
            WorkflowDomainSnapshot workflow,
            WorkspaceStorageIndex storageIndex,
            Set<String> proximateStorageIds
    ) {
        return storageRequest(authority, workflow, storageIndex, true, false, proximateStorageIds);
    }

    private static WorkspaceProjectionRequest storageRequestWithResolverProximateAndDepositIds(
            InventoryAuthoritySnapshot authority,
            WorkflowDomainSnapshot workflow,
            WorkspaceStorageIndex storageIndex,
            Set<String> proximateStorageIds,
            Set<String> depositEligibleStorageIds
    ) {
        return storageRequest(
                authority,
                workflow,
                storageIndex,
                true,
                false,
                proximateStorageIds,
                depositEligibleStorageIds);
    }

    private static WorkspaceProjectionRequest storageRequestWithResolverProximateDepositAndLiveHooks(
            InventoryAuthoritySnapshot authority,
            WorkflowDomainSnapshot workflow,
            WorkspaceStorageIndex storageIndex,
            Set<String> proximateStorageIds,
            Set<String> depositEligibleStorageIds,
            DepositPlanner.ChestContentPresence liveChestContentPresence,
            DepositPlanner.ChestEligibility liveStorageAffinityEligibility
    ) {
        return storageRequest(
                authority,
                workflow,
                storageIndex,
                true,
                false,
                proximateStorageIds,
                depositEligibleStorageIds,
                liveChestContentPresence,
                liveStorageAffinityEligibility);
    }

    private static WorkspaceProjectionRequest storageRequest(
            InventoryAuthoritySnapshot authority,
            WorkflowDomainSnapshot workflow,
            WorkspaceStorageIndex storageIndex,
            boolean includeContentsResolver
    ) {
        return storageRequest(authority, workflow, storageIndex, includeContentsResolver, false);
    }

    private static WorkspaceProjectionRequest storageRequest(
            InventoryAuthoritySnapshot authority,
            WorkflowDomainSnapshot workflow,
            WorkspaceStorageIndex storageIndex,
            boolean includeContentsResolver,
            boolean includeDisplaySources
    ) {
        return storageRequest(authority, workflow, storageIndex, includeContentsResolver, includeDisplaySources, Set.of());
    }

    private static WorkspaceProjectionRequest storageRequest(
            InventoryAuthoritySnapshot authority,
            WorkflowDomainSnapshot workflow,
            WorkspaceStorageIndex storageIndex,
            boolean includeContentsResolver,
            boolean includeDisplaySources,
            Set<String> proximateStorageIds
    ) {
        return storageRequest(
                authority,
                workflow,
                storageIndex,
                includeContentsResolver,
                includeDisplaySources,
                proximateStorageIds,
                Set.of());
    }

    private static WorkspaceProjectionRequest storageRequest(
            InventoryAuthoritySnapshot authority,
            WorkflowDomainSnapshot workflow,
            WorkspaceStorageIndex storageIndex,
            boolean includeContentsResolver,
            boolean includeDisplaySources,
            Set<String> proximateStorageIds,
            Set<String> depositEligibleStorageIds
    ) {
        return storageRequest(
                authority,
                workflow,
                storageIndex,
                includeContentsResolver,
                includeDisplaySources,
                proximateStorageIds,
                depositEligibleStorageIds,
                null,
                null);
    }

    private static WorkspaceProjectionRequest storageRequest(
            InventoryAuthoritySnapshot authority,
            WorkflowDomainSnapshot workflow,
            WorkspaceStorageIndex storageIndex,
            boolean includeContentsResolver,
            boolean includeDisplaySources,
            Set<String> proximateStorageIds,
            Set<String> depositEligibleStorageIds,
            DepositPlanner.ChestContentPresence liveChestContentPresence,
            DepositPlanner.ChestEligibility liveStorageAffinityEligibility
    ) {
        return storageRequest(
                authority,
                workflow,
                storageIndex,
                includeContentsResolver,
                includeDisplaySources,
                proximateStorageIds,
                depositEligibleStorageIds,
                liveChestContentPresence,
                liveStorageAffinityEligibility,
                null);
    }

    private static WorkspaceProjectionRequest storageRequest(
            InventoryAuthoritySnapshot authority,
            WorkflowDomainSnapshot workflow,
            WorkspaceStorageIndex storageIndex,
            boolean includeContentsResolver,
            boolean includeDisplaySources,
            Set<String> proximateStorageIds,
            Set<String> depositEligibleStorageIds,
            DepositPlanner.ChestContentPresence liveChestContentPresence,
            DepositPlanner.ChestEligibility liveStorageAffinityEligibility,
            RemoteStorageDetailIntent remoteStorageDetailIntent
    ) {
        return storageRequest(
                authority,
                workflow,
                storageIndex,
                includeContentsResolver,
                includeDisplaySources,
                proximateStorageIds,
                depositEligibleStorageIds,
                liveChestContentPresence,
                liveStorageAffinityEligibility,
                "",
                remoteStorageDetailIntent);
    }

    private static WorkspaceProjectionRequest storageRequest(
            InventoryAuthoritySnapshot authority,
            WorkflowDomainSnapshot workflow,
            WorkspaceStorageIndex storageIndex,
            boolean includeContentsResolver,
            boolean includeDisplaySources,
            Set<String> proximateStorageIds,
            Set<String> depositEligibleStorageIds,
            DepositPlanner.ChestContentPresence liveChestContentPresence,
            DepositPlanner.ChestEligibility liveStorageAffinityEligibility,
            String searchQuery,
            RemoteStorageDetailIntent remoteStorageDetailIntent
    ) {
        return storageRequest(
                authority,
                workflow,
                storageIndex,
                includeContentsResolver,
                includeDisplaySources,
                proximateStorageIds,
                depositEligibleStorageIds,
                liveChestContentPresence,
                liveStorageAffinityEligibility,
                searchQuery,
                remoteStorageDetailIntent,
                Set.of());
    }

    private static WorkspaceProjectionRequest storageRequest(
            InventoryAuthoritySnapshot authority,
            WorkflowDomainSnapshot workflow,
            WorkspaceStorageIndex storageIndex,
            boolean includeContentsResolver,
            boolean includeDisplaySources,
            Set<String> proximateStorageIds,
            Set<String> depositEligibleStorageIds,
            DepositPlanner.ChestContentPresence liveChestContentPresence,
            DepositPlanner.ChestEligibility liveStorageAffinityEligibility,
            String searchQuery,
            RemoteStorageDetailIntent remoteStorageDetailIntent,
            Set<ItemIdentity> remoteDetailIdentities
    ) {
        WorkspaceStorageIndex resolved = storageIndex == null ? WorkspaceStorageIndex.empty() : storageIndex;
        return new WorkspaceProjectionRequest(
                authority,
                workflow,
                "ready",
                "",
                0,
                0,
                0,
                null,
                null,
                includeContentsResolver ? resolved::contents : null,
                proximateStorageIds == null ? Set.of() : proximateStorageIds,
                null,
                null,
                searchQuery,
                remoteStorageDetailIntent,
                remoteDetailIdentities,
                0L,
                SlotWorkspaceViewModel.ActiveChestPanel.empty(),
                includeDisplaySources ? resolved.displaySources() : List.of(),
                Set.of(),
                List.of(),
                resolved.liveTrackedDisplayEntries(),
                depositEligibleStorageIds == null ? Set.of() : depositEligibleStorageIds,
                resolved,
                liveChestContentPresence,
                liveStorageAffinityEligibility);
    }

    private static InventoryAuthoritySnapshot authority(String itemId, int count) {
        return authority(itemId, "", count);
    }

    private static InventoryAuthoritySnapshot authority(String itemId, String componentFingerprint, int count) {
        return authority(List.of(stack(0, itemId, componentFingerprint, count)));
    }

    private static InventoryAuthoritySnapshot authority(List<InventoryStackSnapshot> entries) {
        return InventoryAuthorityFixtures.authority(
                host(),
                Map.of(BuiltinInventoryIds.PLAYER_MAIN, entries),
                Map.of(BuiltinInventoryIds.PLAYER_MAIN, 36));
    }

    private static InventoryAuthoritySnapshot withCursor(InventoryAuthoritySnapshot authority, ItemStack cursorStack) {
        InventoryAuthoritySnapshot resolved = authority == null ? InventoryAuthoritySnapshot.empty() : authority;
        return new InventoryAuthoritySnapshot(
                resolved.host(),
                resolved.sourcesById(),
                new CursorStateSnapshot(cursorStack, ""));
    }

    private static InventoryStackSnapshot stack(int slotIndex, String itemId, int count) {
        return stack(slotIndex, itemId, "", count);
    }

    private static InventoryStackSnapshot stack(int slotIndex, String itemId, String componentFingerprint, int count) {
        return new InventoryStackSnapshot(
                slotIndex,
                new ItemStack(itemId, componentFingerprint, count, 64),
                count);
    }

    private static SlotWorkspaceViewModel.AtlasItem card(SlotWorkspaceViewModel viewModel, String itemId) {
        SlotWorkspaceViewModel.AtlasItem item = findCard(viewModel, itemId);
        if (item == null) {
            throw new AssertionError("missing card " + itemId);
        }
        return item;
    }

    private static String parityDebug(WorkspaceProjectionParityHarness.Result parity) {
        if (parity == null) {
            return "missing parity result";
        }
        return "invalidated=" + projectionDebug(parity.invalidated())
                + "\nfull=" + projectionDebug(parity.full());
    }

    private static String projectionDebug(WorkspaceProjectionResult result) {
        if (result == null) {
            return "null";
        }
        WorkspaceProjectionResult resolved = result;
        return "{fingerprint=" + resolved.contentFingerprint()
                + ", fullReason=" + resolved.diagnostics().fullProjectionReason()
                + ", projectNanos=" + resolved.diagnostics().timing().projectNanos()
                + ", frame=" + resolved.viewModel().status()
                + '/' + resolved.viewModel().diagnostics()
                + '/' + resolved.viewModel().pendingCount()
                + '/' + resolved.viewModel().selectedQuickAccessSlot()
                + ", atlas=" + cardsDebug(resolved.viewModel().atlasItems())
                + ", triage=" + cardsDebug(resolved.viewModel().triageItems())
                + ", islands=" + resolved.viewModel().islands()
                + ", hotbar=" + resolved.viewModel().hotbarSlots()
                + ", offhand=" + resolved.viewModel().offhand()
                + ", recent=" + resolved.viewModel().recentIdentities()
                + ", wayfinding=" + resolved.viewModel().wayfindingTargets()
                + ", depositable=" + resolved.viewModel().depositableIdentities()
                + ", kits=" + resolved.viewModel().kits()
                + ", craftRun=" + resolved.viewModel().craftRun()
                + ", loot=" + resolved.viewModel().lootChestPanel()
                + ", active=" + resolved.viewModel().activeChestPanel()
                + ", contextual=" + resolved.viewModel().contextualSuggestionLanes()
                + "}";
    }

    private static String cardsDebug(List<SlotWorkspaceViewModel.AtlasItem> items) {
        if (items == null || items.isEmpty()) {
            return "[]";
        }
        StringBuilder out = new StringBuilder("[");
        for (SlotWorkspaceViewModel.AtlasItem item : items) {
            if (out.length() > 1) {
                out.append(", ");
            }
            out.append(item.identity().itemId())
                    .append("{island=").append(item.islandId())
                    .append(", total=").append(item.totalCount())
                    .append(", carried=").append(item.carried())
                    .append(", ghost=").append(item.ghost())
                    .append(", desired=").append(item.desiredCount())
                    .append(", fromKit=").append(item.desiredCountFromKit())
                    .append(", wanted=").append(item.wantedCount())
                    .append(", junk=").append(item.junk())
                    .append(", accepted=").append(item.acceptedWorkflowInput())
                    .append(", putAway=").append(item.putAwayState())
                    .append(", largest=").append(item.largestCarriedSourceId())
                    .append(':').append(item.largestCarriedSlotIndex())
                    .append('/').append(item.largestCarriedSlotCount())
                    .append('}');
        }
        return out.append(']').toString();
    }

    private static boolean hasCard(SlotWorkspaceViewModel viewModel, String itemId) {
        return findCard(viewModel, itemId) != null;
    }

    private static SlotWorkspaceViewModel.AtlasItem findCard(SlotWorkspaceViewModel viewModel, String itemId) {
        for (SlotWorkspaceViewModel.AtlasItem item : viewModel.atlasItems()) {
            if (item.identity().itemId().equals(itemId)) {
                return item;
            }
        }
        for (SlotWorkspaceViewModel.AtlasItem item : viewModel.triageItems()) {
            if (item.identity().itemId().equals(itemId)) {
                return item;
            }
        }
        return null;
    }

    private static SlotWorkspaceViewModel.ChestChip chip(SlotWorkspaceViewModel viewModel, String storageId) {
        SlotWorkspaceViewModel.ChestChip chip = viewModel.chestChip(storageId);
        if (chip == null) {
            throw new AssertionError("missing storage chip " + storageId);
        }
        return chip;
    }

    private static SlotWorkspaceViewModel.ChestClusterDescriptor cluster(
            SlotWorkspaceViewModel viewModel,
            String clusterId
    ) {
        for (SlotWorkspaceViewModel.ChestClusterDescriptor cluster : viewModel.chestClusters()) {
            if (cluster != null && cluster.clusterId().equals(clusterId)) {
                return cluster;
            }
        }
        throw new AssertionError("missing storage cluster " + clusterId);
    }

    private static boolean hasChip(SlotWorkspaceViewModel viewModel, String storageId) {
        return viewModel.chestChip(storageId) != null;
    }

    private static WorkspaceProjectionStore.CarriedIdentityFact carriedFact(
            WorkspaceProjectionStore store,
            String itemId
    ) {
        for (WorkspaceProjectionStore.CarriedIdentityFact fact : store.carriedIdentities().values()) {
            if (fact.identity() != null && fact.identity().itemId().equals(itemId)) {
                return fact;
            }
        }
        throw new AssertionError("missing carried fact " + itemId);
    }

    private static WorkspaceProjectionStore.StoragePresenceFact storagePresence(
            WorkspaceProjectionStore store,
            String storageId,
            String itemId
    ) {
        for (WorkspaceProjectionStore.StoragePresenceFact fact : store.storagePresence().values()) {
            if (fact.key() != null
                    && fact.key().storageId().equals(storageId)
                    && fact.key().identity() != null
                    && fact.key().identity().itemId().equals(itemId)) {
                return fact;
            }
        }
        throw new AssertionError("missing storage presence " + storageId + " " + itemId);
    }

    private static WorkspaceStorageIndex storageIndex(
            List<WorkspaceStorageIndex.StorageEntry> entries,
            long revision
    ) {
        java.util.LinkedHashMap<String, WorkspaceStorageIndex.StorageEntry> byId = new java.util.LinkedHashMap<>();
        for (WorkspaceStorageIndex.StorageEntry entry : entries) {
            byId.put(entry.target().storageId(), entry);
        }
        return new WorkspaceStorageIndex(byId, Map.of(), List.of(), revision);
    }

    private static WorkspaceStorageIndex displayStorageIndex(
            List<WorldDisplayStorageSource> sources,
            long revision
    ) {
        java.util.LinkedHashMap<String, WorkspaceStorageIndex.StorageEntry> byId = new java.util.LinkedHashMap<>();
        for (WorldDisplayStorageSource source : sources) {
            WorkspaceStorageIndex.StorageEntry entry = displayStorageEntry(source, true);
            byId.put(entry.target().storageId(), entry);
        }
        return new WorkspaceStorageIndex(byId, Map.of(), sources, revision);
    }

    private static WorkspaceStorageIndex.StorageEntry displayStorageEntry(
            String storageId,
            int x,
            String itemId,
            int count
    ) {
        return displayStorageEntry(storageId, x, itemId, count, true);
    }

    private static WorkspaceStorageIndex.StorageEntry remoteDisplayStorageEntry(
            String storageId,
            int x,
            String itemId,
            int count
    ) {
        return displayStorageEntry(storageId, x, itemId, count, false);
    }

    private static WorkspaceStorageIndex.StorageEntry rememberedRemoteDisplayStorageEntry(
            String storageId,
            int x,
            String itemId,
            int count
    ) {
        return displayStorageEntry(storageId, x, itemId, count, false, false, true);
    }

    private static WorkspaceStorageIndex.StorageEntry displayStorageEntry(
            String storageId,
            int x,
            String itemId,
            int count,
            boolean proximate
    ) {
        return displayStorageEntry(storageId, x, itemId, count, proximate, true, false);
    }

    private static WorkspaceStorageIndex.StorageEntry displayStorageEntry(
            String storageId,
            int x,
            String itemId,
            int count,
            boolean proximate,
            boolean live,
            boolean remembered
    ) {
        ItemStack stack = new ItemStack(itemId, "", count, 64);
        return displayStorageEntry(new WorldDisplayStorageSource(
                storageId,
                WorldDisplayStorageKind.TOOL_RACK,
                storageId,
                "minecraft:overworld",
                x,
                64,
                0,
                9,
                List.of(new WorldStorageAccess.SlotContent(0, stack))), proximate, live, remembered);
    }

    private static WorldDisplayStorageSource displayStorageSource(
            String storageId,
            int x,
            String itemId,
            int count
    ) {
        ItemStack stack = new ItemStack(itemId, "", count, 64);
        return new WorldDisplayStorageSource(
                storageId,
                WorldDisplayStorageKind.TOOL_RACK,
                storageId,
                "minecraft:overworld",
                x,
                64,
                0,
                9,
                List.of(new WorldStorageAccess.SlotContent(0, stack)));
    }

    private static WorkspaceStorageIndex.StorageEntry displayStorageEntry(
            WorldDisplayStorageSource source,
            boolean proximate
    ) {
        return displayStorageEntry(source, proximate, true, false);
    }

    private static WorkspaceStorageIndex.StorageEntry displayStorageEntry(
            WorldDisplayStorageSource source,
            boolean proximate,
            boolean live,
            boolean remembered
    ) {
        ItemStack stack = source.contents().isEmpty()
                ? ItemStack.EMPTY
                : source.contents().get(0).stack();
        SlotWorkspaceViewModel.ChestContentsSnapshot snapshot = new SlotWorkspaceViewModel.ChestContentsSnapshot(
                source.slotCount(),
                stack.isEmpty() ? List.of() : List.of(stack),
                List.of(0));
        return new WorkspaceStorageIndex.StorageEntry(
                StorageTargetRef.display(source, remembered, proximate),
                snapshot,
                stack.isEmpty()
                        ? Map.of()
                        : Map.of(new ItemIdentity(stack.itemId(), ItemComparisonMode.ITEM_ID, ""), stack.getCount()),
                live,
                remembered);
    }

    private static WorkspaceStorageIndex.StorageEntry claimedStorageEntry(
            UUID storageId,
            String itemId,
            int count,
            boolean proximate
    ) {
        SlotWorkspaceViewModel.ChestContentsSnapshot snapshot = new SlotWorkspaceViewModel.ChestContentsSnapshot(
                27,
                List.of(new ItemStack(itemId, "", count, 64)),
                List.of(0));
        return new WorkspaceStorageIndex.StorageEntry(
                StorageTargetRef.claimed(
                        storageId,
                        "minecraft:overworld",
                        12,
                        64,
                        -5,
                        "Test Chest",
                        true,
                        false,
                        proximate,
                        false,
                        true),
                snapshot,
                Map.of(new ItemIdentity(itemId, ItemComparisonMode.ITEM_ID, ""), count),
                true,
                false);
    }

    private static WorkspaceStorageIndex.StorageEntry claimedStorageEntryWithTags(
            UUID storageId,
            String itemId,
            int count,
            boolean proximate,
            String tagId
    ) {
        SlotWorkspaceViewModel.ChestContentsSnapshot snapshot = new SlotWorkspaceViewModel.ChestContentsSnapshot(
                27,
                List.of(new ItemStack(itemId, "", count, 64).withTags(tagId)),
                List.of(0));
        return new WorkspaceStorageIndex.StorageEntry(
                StorageTargetRef.claimed(
                        storageId,
                        "minecraft:overworld",
                        12,
                        64,
                        -5,
                        "Test Chest",
                        true,
                        false,
                        proximate,
                        false,
                        true),
                snapshot,
                Map.of(new ItemIdentity(itemId, ItemComparisonMode.ITEM_ID, ""), count),
                true,
                false);
    }

    private static WorkspaceStorageIndex.StorageEntry claimedEmptyStorageEntry(
            UUID storageId,
            boolean proximate
    ) {
        SlotWorkspaceViewModel.ChestContentsSnapshot snapshot = new SlotWorkspaceViewModel.ChestContentsSnapshot(
                27,
                List.of(),
                List.of());
        return new WorkspaceStorageIndex.StorageEntry(
                StorageTargetRef.claimed(
                        storageId,
                        "minecraft:overworld",
                        12,
                        64,
                        -5,
                        "Test Chest",
                        true,
                        false,
                        proximate,
                        false,
                        true),
                snapshot,
                Map.of(),
                true,
                false);
    }

    private static ClaimedChest claimedChest(UUID storageId) {
        return new ClaimedChest(
                storageId,
                Set.of(new ChestAnchor("minecraft:overworld", 12, 64, -5)),
                0,
                0,
                "Test Chest");
    }

    private static ChestAffinityMap affinity(UUID storageId, ItemIdentity identity, int score) {
        return new ChestAffinityMap(Map.of(
                storageId,
                Map.of(identity, new ChestAffinity(identity, score, 0L))));
    }

    private static WorkspaceStorageIndex storageIndex(String itemId, String componentFingerprint, int count) {
        UUID storageId = UUID.fromString("00000000-0000-0000-0000-000000000042");
        SlotWorkspaceViewModel.ChestContentsSnapshot snapshot = new SlotWorkspaceViewModel.ChestContentsSnapshot(
                27,
                List.of(new ItemStack(itemId, componentFingerprint, count, 64)),
                List.of(0));
        WorkspaceStorageIndex.StorageEntry entry = new WorkspaceStorageIndex.StorageEntry(
                StorageTargetRef.claimed(
                        storageId,
                        "minecraft:overworld",
                        1,
                        2,
                        3,
                        "Test Chest",
                        true,
                        false,
                        true),
                snapshot,
                Map.of(new ItemIdentity(itemId, ItemComparisonMode.ITEM_ID_AND_COMPONENTS, componentFingerprint), count),
                true,
                false);
        return new WorkspaceStorageIndex(
                Map.of(storageId.toString(), entry),
                Map.of(),
                List.of(),
                1L);
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
