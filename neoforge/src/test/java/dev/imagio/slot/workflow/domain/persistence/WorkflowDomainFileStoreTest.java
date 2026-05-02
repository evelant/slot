package dev.imagio.slot.workflow.domain.persistence;

import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.inventory.action.InventoryActionScope;
import dev.imagio.slot.inventory.browse.InventoryBrowseFilter;
import dev.imagio.slot.inventory.browse.InventoryBrowseFilterScope;
import dev.imagio.slot.inventory.browse.InventoryBrowseGroupingMode;
import dev.imagio.slot.inventory.browse.InventoryBrowsePaneMode;
import dev.imagio.slot.inventory.browse.InventoryBrowseSessionState;
import dev.imagio.slot.inventory.browse.InventoryBrowseSortMode;
import dev.imagio.slot.inventory.browse.InventoryBrowseSubjectRef;
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.InventoryPaneMembership;
import dev.imagio.slot.workflow.domain.ChestAnchor;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import dev.imagio.slot.workflow.domain.CollectionDefinition;
import dev.imagio.slot.workflow.domain.KitDefinition;
import dev.imagio.slot.workflow.domain.KitPage;
import dev.imagio.slot.workflow.domain.InMemoryWorkflowDomainStateRepository;
import dev.imagio.slot.workflow.domain.InventoryActivityConfidence;
import dev.imagio.slot.workflow.domain.InventoryActivityEvent;
import dev.imagio.slot.workflow.domain.InventoryActivityKind;
import dev.imagio.slot.workflow.domain.InventoryActivityProducer;
import dev.imagio.slot.workflow.domain.LoadoutTarget;
import dev.imagio.slot.workflow.domain.QuickAccessLoadoutEntry;
import dev.imagio.slot.workflow.domain.QuickAccessLoadoutDefinition;
import dev.imagio.slot.workflow.domain.VisualAtlasIsland;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import dev.imagio.slot.workflow.domain.WorkflowDomainPersistenceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

import dev.imagio.slot.workflow.domain.VisualHomeAssignment;
import dev.imagio.slot.workflow.domain.VisualHomeMap;
import dev.imagio.slot.workflow.domain.WorkflowDomainSnapshot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkflowDomainFileStoreTest {
    @TempDir
    Path tempDir;

    @Test
    void fileStoreRoundTripsWorkflowDomainSnapshot() {
        InMemoryWorkflowDomainStateRepository source = new InMemoryWorkflowDomainStateRepository();
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(source, null);
        CollectionDefinition exploration = runtime.collectionWorkflow().createCollection("Exploration");
        runtime.collectionWorkflow().toggleCollectionMembership(ItemIdentity.of("minecraft:torch"), exploration.id());
        // Player-scoped desired count (collection-scoped variant retired
        // alongside the kits replacement of collections).
        runtime.desiredCountWorkflow().setPlayer(ItemIdentity.of("minecraft:torch"), 48);
        QuickAccessLoadoutDefinition loadout = runtime.collectionWorkflow().createLoadout(
                exploration.id(),
                "Caving",
                Set.of(
                        new QuickAccessLoadoutEntry(
                                new LoadoutTarget.QuickAccessLaneTarget(BuiltinInventoryIds.QUICK_ACCESS_LANE_0, 0),
                                ItemIdentity.of("minecraft:torch")
                        ),
                        new QuickAccessLoadoutEntry(
                                new LoadoutTarget.EquipmentSlotTarget(BuiltinInventoryIds.EQUIPMENT_GROUP_OFFHAND, 0),
                                ItemIdentity.of("minecraft:shield")
                        )
                )
        );
        runtime.recordActivityEvent(new InventoryActivityEvent(
                InventoryActivityKind.ACQUIRED,
                InventoryActivityProducer.WORLD_PICKUP,
                InventoryActivityConfidence.OBSERVED,
                ItemIdentity.of("minecraft:diamond"),
                2,
                null,
                null,
                "",
                "",
                java.util.List.of(),
                ""
        ));
        runtime.setProtectedIdentity(ItemIdentity.of("minecraft:shield"), true);
        runtime.setProtectedTarget(
                new InventoryActionTarget.EquipmentTarget(BuiltinInventoryIds.EQUIPMENT_GROUP_OFFHAND, 0),
                true
        );
        runtime.setProtectPortableContainers(true);
        VisualAtlasIsland island = runtime.visualAtlasWorkflow().createIsland(
                "Machines",
                744,
                104,
                0xCC5A4A6E,
                ItemIdentity.of("minecraft:torch")
        );
        runtime.visualAtlasWorkflow().assignHome(ItemIdentity.of("minecraft:torch"), island.id(), 0);
        runtime.visualAtlasWorkflow().moveIsland(island.id(), 912, 236);
        source.browseSessionState().replaceWith(new InventoryBrowseSessionState(
                new InventoryBrowseFilter("torch", InventoryBrowseFilterScope.SELECTED_COLLECTION),
                InventoryBrowseSortMode.COUNT_DESC,
                InventoryBrowseGroupingMode.SOURCE,
                InventoryBrowsePaneMode.DUAL_PANE,
                InventoryPaneMembership.EXTERNAL,
                exploration.id(),
                loadout.id(),
                "tool:craft",
                InventoryActionScope.VISIBLE_ROWS,
                new InventoryBrowseSubjectRef.LoadoutRef(exploration.id(), loadout.id()),
                Set.of("exploration=collapsed")
        ));

        WorkflowDomainFileStore fileStore = new WorkflowDomainFileStore(tempDir.resolve("slot-workflow-state.json"));
        WorkflowDomainPersistenceService service = new WorkflowDomainPersistenceService(fileStore);
        service.saveFrom(source);

        InMemoryWorkflowDomainStateRepository restored = new InMemoryWorkflowDomainStateRepository();
        service.loadInto(restored);

        assertEquals(source.snapshot(), restored.snapshot());
        assertEquals("torch", restored.browseSessionState().current().filter().searchText());
        assertEquals(
                new InventoryBrowseSubjectRef.LoadoutRef(exploration.id(), loadout.id()),
                restored.browseSessionState().current().selectedSubject()
        );
        assertTrue(restored.workflowProjection().protection().protects(ItemIdentity.of("minecraft:shield"), null));
        assertTrue(restored.workflowProjection().protection().protectsPortableContainers());
    }

    @Test
    void fileStoreRoundTripsIslandManagementEvents() {
        InMemoryWorkflowDomainStateRepository source = new InMemoryWorkflowDomainStateRepository();
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(source, null);

        VisualAtlasIsland keeper = runtime.visualAtlasWorkflow().createIsland(
                "Machines", 10, 20, 0xCC5A4A6E, ItemIdentity.of("minecraft:torch")
        );
        runtime.visualAtlasWorkflow().renameIsland(keeper.id(), "Workshop");
        runtime.visualAtlasWorkflow().recolorIsland(keeper.id(), 0xFF112233);
        runtime.visualAtlasWorkflow().setIslandIcon(keeper.id(), ItemIdentity.of("minecraft:anvil"));

        VisualAtlasIsland doomed = runtime.visualAtlasWorkflow().createIsland(
                "Scraps", 40, 60, 0xFF222222, null
        );
        runtime.visualAtlasWorkflow().deleteIsland(doomed.id());

        runtime.visualAtlasWorkflow().dismissTemplate("template.food");

        WorkflowDomainFileStore fileStore = new WorkflowDomainFileStore(tempDir.resolve("slot-island-mgmt.json"));
        WorkflowDomainPersistenceService service = new WorkflowDomainPersistenceService(fileStore);
        service.saveFrom(source);

        InMemoryWorkflowDomainStateRepository restored = new InMemoryWorkflowDomainStateRepository();
        service.loadInto(restored);

        assertEquals(source.snapshot(), restored.snapshot());
        VisualAtlasIsland restoredKeeper = restored.workflowProjection().visualHomeMap().island(keeper.id());
        assertEquals("Workshop", restoredKeeper.label());
        assertEquals(0xFF112233, restoredKeeper.color());
        assertEquals(ItemIdentity.of("minecraft:anvil"), restoredKeeper.iconIdentity());
        assertTrue(restored.workflowProjection().visualHomeMap().island(doomed.id()) == null);
        assertTrue(restored.workflowProjection().visualHomeMap().templateDismissed("template.food"));
    }

    @Test
    void fileStoreRoundTripsClaimedChestsAndAffinity() {
        InMemoryWorkflowDomainStateRepository source = new InMemoryWorkflowDomainStateRepository();
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(source, null);

        ChestAnchor primaryAnchor = new ChestAnchor("minecraft:overworld", 100, 64, 200);
        ChestAnchor pairedAnchor = new ChestAnchor("minecraft:overworld", 101, 64, 200);
        ClaimedChest kept = runtime.chestClaimWorkflow().claim(
                Set.of(primaryAnchor, pairedAnchor),
                2400,
                0,
                "Base Machines"
        );
        ClaimedChest alsoKept = runtime.chestClaimWorkflow().claim(
                Set.of(new ChestAnchor("minecraft:overworld", 120, 64, 200)),
                2560,
                0,
                ""
        );
        runtime.chestClaimWorkflow().relabelChest(alsoKept.storageId(), "Pantry");
        runtime.chestClaimWorkflow().moveChest(alsoKept.storageId(), 2720, 160);

        ClaimedChest doomed = runtime.chestClaimWorkflow().claim(
                Set.of(new ChestAnchor("minecraft:overworld", 200, 64, 200)),
                2600,
                320,
                "Scrap"
        );
        runtime.chestClaimWorkflow().deleteChest(doomed.storageId());

        // Affinity replaces the link: bump it for kept/alsoKept identities.
        ItemIdentity redstone = ItemIdentity.of("minecraft:redstone");
        ItemIdentity bread = ItemIdentity.of("minecraft:bread");
        runtime.chestClaimWorkflow().recordDeposit(kept.storageId(), redstone, 1, 100L);
        runtime.chestClaimWorkflow().recordDeposit(kept.storageId(), redstone, 1, 110L);
        runtime.chestClaimWorkflow().recordDeposit(alsoKept.storageId(), bread, 4, 120L);

        UUID keptId = kept.storageId();
        UUID alsoKeptId = alsoKept.storageId();

        WorkflowDomainFileStore fileStore = new WorkflowDomainFileStore(tempDir.resolve("slot-storage.json"));
        WorkflowDomainPersistenceService service = new WorkflowDomainPersistenceService(fileStore);
        service.saveFrom(source);

        InMemoryWorkflowDomainStateRepository restored = new InMemoryWorkflowDomainStateRepository();
        service.loadInto(restored);

        assertEquals(source.snapshot(), restored.snapshot());

        ClaimedChest restoredKept = restored.workflowProjection().claimedChestMap().chest(keptId);
        assertEquals("Base Machines", restoredKept.label());
        assertEquals(2, restoredKept.anchors().size());

        ClaimedChest restoredPantry = restored.workflowProjection().claimedChestMap().chest(alsoKeptId);
        assertEquals("Pantry", restoredPantry.label());
        assertEquals(2720, restoredPantry.atlasX());

        assertTrue(restored.workflowProjection().claimedChestMap().chest(doomed.storageId()) == null);

        assertEquals(2, restored.workflowProjection().chestAffinityMap().score(keptId, redstone));
        assertEquals(1, restored.workflowProjection().chestAffinityMap().score(alsoKeptId, bread));
    }

    @Test
    void fileStoreRoundTripsKitsAndActivation() {
        InMemoryWorkflowDomainStateRepository source = new InMemoryWorkflowDomainStateRepository();
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(source, null);

        KitDefinition mining = runtime.kitWorkflow().create("Mining");
        KitPage page0 = KitPage.empty()
                .withSlot(0, ItemIdentity.of("minecraft:iron_pickaxe"))
                .withSlot(1, ItemIdentity.of("minecraft:torch"));
        KitPage page1 = KitPage.empty().withSlot(0, ItemIdentity.of("minecraft:iron_shovel"));
        runtime.kitWorkflow().update(mining.withPages(java.util.List.of(page0, page1))
                .withOffhand(ItemIdentity.of("minecraft:shield"))
        );
        // Bring list folded into kit-scoped desired counts. Seed two
        // identities so the round-trip exercises the kit-scoped persistence.
        runtime.desiredCountWorkflow().setForKit(mining.id(), ItemIdentity.of("minecraft:cobblestone"), 1);
        runtime.desiredCountWorkflow().setForKit(mining.id(), ItemIdentity.of("minecraft:bread"), 1);
        KitDefinition combat = runtime.kitWorkflow().create("Combat");
        runtime.kitWorkflow().update(combat.withPages(java.util.List.of(
                KitPage.empty().withSlot(0, ItemIdentity.of("minecraft:iron_sword"))
        )));
        runtime.kitWorkflow().activate(mining.id());
        runtime.kitWorkflow().switchPage(1);

        WorkflowDomainFileStore fileStore = new WorkflowDomainFileStore(tempDir.resolve("slot-kits.json"));
        WorkflowDomainPersistenceService service = new WorkflowDomainPersistenceService(fileStore);
        service.saveFrom(source);

        InMemoryWorkflowDomainStateRepository restored = new InMemoryWorkflowDomainStateRepository();
        service.loadInto(restored);

        assertEquals(source.snapshot(), restored.snapshot());
        assertEquals(2, restored.workflowProjection().kitMap().kits().size());
        KitDefinition restoredMining = restored.workflowProjection().kitMap().kit(mining.id());
        assertEquals(2, restoredMining.pageCount());
        assertEquals(ItemIdentity.of("minecraft:iron_pickaxe"), restoredMining.page(0).slot(0));
        assertEquals(ItemIdentity.of("minecraft:iron_shovel"), restoredMining.page(1).slot(0));
        assertEquals(ItemIdentity.of("minecraft:shield"), restoredMining.offhand());
        // Bring list lives on as kit-scoped desired counts.
        java.util.Map<ItemIdentity, Integer> restoredKitWants = restored.workflowProjection()
                .kitDesiredCounts().getOrDefault(mining.id(), java.util.Map.of());
        assertEquals(1, restoredKitWants.getOrDefault(ItemIdentity.of("minecraft:cobblestone"), 0));
        assertEquals(1, restoredKitWants.getOrDefault(ItemIdentity.of("minecraft:bread"), 0));
        assertEquals(mining.id(), restored.workflowProjection().kitMap().activation().kitId());
        assertEquals(1, restored.workflowProjection().kitMap().activation().pageIndex());
    }

    @Test
    void preTwoTwoFileMigratesLegacyCoordsIntoOrdinals() throws Exception {
        // Hand-craft a v5 file with the freeform-coordinate visual-home shape:
        // three identities homed to one island, sorted by (y, x) so the
        // expected post-migration ordinals are stone=0, iron=1, gold=2.
        String legacyJson = """
                {
                  "version": 5,
                  "nextGlobalSequence": 1,
                  "workflowNextStreamSequence": 1,
                  "activityNextStreamSequence": 1,
                  "activityMaxEvents": 0,
                  "workflowCheckpoint": {
                    "userCollections": [],
                    "memberships": [],
                    "desiredCounts": [],
                    "loadouts": [],
                    "favorites": [],
                    "junk": [],
                    "protection": {"identities": [], "targets": [], "protectPortableContainers": false},
                    "recentDismissals": [],
                    "visualIslands": [
                      {"id": "machines", "label": "Machines", "kind": "PLAYER",
                        "x": 100, "y": 100, "width": 320, "height": 196,
                        "color": -858993460, "iconIdentity": null}
                    ],
                    "visualHomes": [
                      {"identity": {"itemId": "minecraft:iron_ingot",
                                    "comparisonMode": "ITEM_ID", "componentFingerprint": ""},
                       "islandId": "machines", "x": 8, "y": 40,
                       "origin": "PLAYER_PLACED", "locked": true},
                      {"identity": {"itemId": "minecraft:gold_ingot",
                                    "comparisonMode": "ITEM_ID", "componentFingerprint": ""},
                       "islandId": "machines", "x": 8, "y": 76,
                       "origin": "PLAYER_PLACED", "locked": true},
                      {"identity": {"itemId": "minecraft:stone",
                                    "comparisonMode": "ITEM_ID", "componentFingerprint": ""},
                       "islandId": "machines", "x": 8, "y": 8,
                       "origin": "PLAYER_PLACED", "locked": true}
                    ],
                    "dismissedTemplateIds": [],
                    "claimedChests": [],
                    "chestLinks": [],
                    "kits": [],
                    "kitActivation": null
                  },
                  "workflowEvents": [],
                  "activityCheckpoint": null,
                  "activityEvents": []
                }
                """;
        Path stateFile = tempDir.resolve("slot-legacy.json");
        Files.writeString(stateFile, legacyJson);

        WorkflowDomainFileStore store = new WorkflowDomainFileStore(stateFile);
        WorkflowDomainSnapshot loaded = store.load();
        VisualHomeMap map = loaded.workflowProjection().visualHomeMap();

        VisualHomeAssignment stone = map.assignment(ItemIdentity.of("minecraft:stone"));
        VisualHomeAssignment iron = map.assignment(ItemIdentity.of("minecraft:iron_ingot"));
        VisualHomeAssignment gold = map.assignment(ItemIdentity.of("minecraft:gold_ingot"));
        assertNotNull(stone);
        assertNotNull(iron);
        assertNotNull(gold);
        assertEquals(0, stone.ordinal(), "stone is the topmost legacy home → ordinal 0");
        assertEquals(1, iron.ordinal(), "iron sits between stone and gold → ordinal 1");
        assertEquals(2, gold.ordinal(), "gold is bottommost → ordinal 2");
    }
}
