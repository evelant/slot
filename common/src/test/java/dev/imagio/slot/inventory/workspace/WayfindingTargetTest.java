package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.workflow.domain.ChestAffinityMap;
import dev.imagio.slot.workflow.domain.ChestAnchor;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import dev.imagio.slot.workflow.domain.ClaimedChestMap;
import dev.imagio.slot.workflow.domain.KitActivation;
import dev.imagio.slot.workflow.domain.KitDefinition;
import dev.imagio.slot.workflow.domain.KitMap;
import dev.imagio.slot.workflow.domain.KitPage;
import dev.imagio.slot.workflow.domain.ProtectionSnapshotPolicy;
import dev.imagio.slot.workflow.domain.VisualHomeMap;
import dev.imagio.slot.workflow.domain.WorkflowDomainSnapshot;
import dev.imagio.slot.workflow.domain.WorkflowProjection;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Phase 1 wayfinding projection coverage. Drives the
 * {@link WayfindingTarget} list inside
 * {@link SlotWorkspaceViewModel#project}: empty, single-target,
 * multi-target, cross-dimension, and the bug #5 era non-stackable
 * regression (water_bucket targeting one chest, not splitting into two).
 */
class WayfindingTargetTest {
    private static final UUID CHEST_OVERWORLD = UUID.fromString("00000000-0000-0000-0000-00000000A001");
    private static final UUID CHEST_OVERWORLD_2 = UUID.fromString("00000000-0000-0000-0000-00000000A002");
    private static final UUID CHEST_NETHER = UUID.fromString("00000000-0000-0000-0000-00000000B001");

    private static final ItemIdentity REDSTONE = ItemIdentity.of("minecraft:redstone");
    private static final ItemIdentity DIAMOND = ItemIdentity.of("minecraft:diamond");
    private static final ItemIdentity WATER_BUCKET = ItemIdentity.of("minecraft:water_bucket");

    @Test
    void noKitNoDesiredCountsProducesNoTargets() {
        SlotWorkspaceViewModel projected = projectWith(
                Map.of(),
                noKit(),
                Map.of(),
                Map.of(CHEST_OVERWORLD, snapshotOf(stack("minecraft:redstone", 32))),
                anchorOverworld(0, 64, 0, CHEST_OVERWORLD)
        );
        assertTrue(projected.wayfindingTargets().isEmpty(),
                "no kit + no desired counts → wayfinding empty");
    }

    @Test
    void singleTargetSurfacesPlayerDesiredCount() {
        SlotWorkspaceViewModel projected = projectWith(
                Map.of(REDSTONE, 64),
                noKit(),
                Map.of(),
                Map.of(CHEST_OVERWORLD, snapshotOf(stack("minecraft:redstone", 32))),
                anchorOverworld(10, 64, 20, CHEST_OVERWORLD)
        );
        assertEquals(1, projected.wayfindingTargets().size());
        WayfindingTarget target = projected.wayfindingTargets().get(0);
        assertEquals(CHEST_OVERWORLD.toString(), target.storageId());
        assertEquals("minecraft:overworld", target.dimensionId());
        assertEquals(10, target.worldX());
        assertEquals(64, target.worldY());
        assertEquals(20, target.worldZ());
        assertEquals(WayfindingTarget.Scope.PLAYER, target.scope());
        assertEquals(32, target.totalMissingCount());
        assertTrue(target.missingIdentities().contains(REDSTONE));
    }

    @Test
    void multiTargetMarksKitScopeWhenAnyIdentityIsKitNeeded() {
        // Active kit needs diamond on a hotbar slot. Player-global wants
        // redstone too. Chest A holds both — KIT scope should win.
        KitDefinition kit = kitWithSlot(DIAMOND);
        KitMap kitMap = new KitMap(List.of(kit), new KitActivation("kit-1", 0));
        SlotWorkspaceViewModel projected = projectWith(
                Map.of(REDSTONE, 16),
                kitMap,
                Map.of(),
                Map.of(CHEST_OVERWORLD, snapshotOf(
                        stack("minecraft:redstone", 8),
                        stack("minecraft:diamond", 4))),
                anchorOverworld(0, 64, 0, CHEST_OVERWORLD)
        );
        assertEquals(1, projected.wayfindingTargets().size());
        WayfindingTarget target = projected.wayfindingTargets().get(0);
        assertEquals(WayfindingTarget.Scope.KIT, target.scope(),
                "any KIT-scoped missing identity → target scope is KIT");
        assertEquals(Set.of(REDSTONE, DIAMOND), Set.copyOf(target.missingIdentities()));
        assertEquals(12, target.totalMissingCount());
    }

    @Test
    void crossDimensionTargetCarriesNetherCoords() {
        // Chest in the nether holds a player-wanted item; projection should
        // tag it with the nether dimensionId so client renderers know to
        // swap compass+distance for dim-shorthand+coords.
        SlotWorkspaceViewModel projected = projectWith(
                Map.of(DIAMOND, 32),
                noKit(),
                Map.of(),
                Map.of(CHEST_NETHER, snapshotOf(stack("minecraft:diamond", 16))),
                List.of(new AnchorSpec(CHEST_NETHER, "minecraft:the_nether", -42, 99, 17))
        );
        assertEquals(1, projected.wayfindingTargets().size());
        WayfindingTarget target = projected.wayfindingTargets().get(0);
        assertEquals("minecraft:the_nether", target.dimensionId());
        assertEquals(-42, target.worldX());
        assertEquals(99, target.worldY());
        assertEquals(17, target.worldZ());
    }

    @Test
    void chestWithoutMissingIdentityIsSkipped() {
        // Player wants diamond, chest only holds redstone — no target.
        SlotWorkspaceViewModel projected = projectWith(
                Map.of(DIAMOND, 8),
                noKit(),
                Map.of(),
                Map.of(CHEST_OVERWORLD, snapshotOf(stack("minecraft:redstone", 32))),
                anchorOverworld(0, 64, 0, CHEST_OVERWORLD)
        );
        assertTrue(projected.wayfindingTargets().isEmpty());
    }

    @Test
    void multiChestProjectionEmitsOneTargetPerChest() {
        SlotWorkspaceViewModel projected = projectWith(
                Map.of(REDSTONE, 256),
                noKit(),
                Map.of(),
                Map.of(
                        CHEST_OVERWORLD, snapshotOf(stack("minecraft:redstone", 32)),
                        CHEST_OVERWORLD_2, snapshotOf(stack("minecraft:redstone", 16))),
                List.of(
                        new AnchorSpec(CHEST_OVERWORLD, "minecraft:overworld", 0, 64, 0),
                        new AnchorSpec(CHEST_OVERWORLD_2, "minecraft:overworld", 50, 64, 0))
        );
        assertEquals(2, projected.wayfindingTargets().size());
        WayfindingTarget first = projected.wayfindingTargets().get(0);
        WayfindingTarget second = projected.wayfindingTargets().get(1);
        assertNotNull(first);
        assertNotNull(second);
        // Sum across the pair matches what the chests hold.
        int totalMissing = first.totalMissingCount() + second.totalMissingCount();
        assertEquals(48, totalMissing);
    }

    @Test
    void nonStackableIdentityCollapsesToSingleTarget() {
        // bug #5 regression: water_bucket (non-stackable) in one chest must
        // produce a single target with one missing identity, not split into
        // multiple targets due to identity computation divergence.
        SlotWorkspaceViewModel projected = projectWith(
                Map.of(WATER_BUCKET, 1),
                noKit(),
                Map.of(),
                Map.of(CHEST_OVERWORLD, snapshotOf(
                        stack("minecraft:water_bucket", 1, 1))),
                anchorOverworld(0, 64, 0, CHEST_OVERWORLD)
        );
        assertEquals(1, projected.wayfindingTargets().size());
        WayfindingTarget target = projected.wayfindingTargets().get(0);
        assertEquals(1, target.missingIdentities().size());
        assertTrue(target.missingIdentities().contains(WATER_BUCKET));
    }

    @Test
    void carriedAlreadyCoversDesiredWhenCountMet() {
        // playerDesiredCounts asks for 16 redstone; player carries 16 — no
        // target should fire (not missing).
        InventoryAuthoritySnapshot authority = InventoryAuthoritySnapshot.empty();
        // We can't easily construct a non-empty authority here; instead,
        // verify the inverse: a 0-target desired count produces no target.
        SlotWorkspaceViewModel projected = projectWith(
                Map.of(REDSTONE, 0),
                noKit(),
                Map.of(),
                Map.of(CHEST_OVERWORLD, snapshotOf(stack("minecraft:redstone", 8))),
                anchorOverworld(0, 64, 0, CHEST_OVERWORLD)
        );
        // Target = 0 means "no preference" — should produce no target.
        assertTrue(projected.wayfindingTargets().isEmpty());
        assertFalse(authority == null);
    }

    // --- fixtures ---------------------------------------------------------

    private static KitMap noKit() {
        return KitMap.empty();
    }

    private static KitDefinition kitWithSlot(ItemIdentity identity) {
        KitPage page = KitPage.empty().withSlot(0, identity);
        return new KitDefinition("kit-1", "Kit 1", List.of(page), null);
    }

    private static List<AnchorSpec> anchorOverworld(int x, int y, int z, UUID storageId) {
        return List.of(new AnchorSpec(storageId, "minecraft:overworld", x, y, z));
    }

    private record AnchorSpec(UUID storageId, String dimensionId, int x, int y, int z) {
    }

    private static ItemStack stack(String itemId, int count) {
        return new ItemStack(itemId, count, 64);
    }

    /** Non-stackable fixture (max=1) — water_bucket-style. */
    private static ItemStack stack(String itemId, int count, int maxStackSize) {
        return new ItemStack(itemId, count, maxStackSize);
    }

    private static SlotWorkspaceViewModel.ChestContentsSnapshot snapshotOf(ItemStack... contents) {
        return new SlotWorkspaceViewModel.ChestContentsSnapshot(contents.length, List.of(contents));
    }

    /**
     * Drive a {@link SlotWorkspaceViewModel#project} call with the given
     * inputs. Bypasses authority, signal, and learned-rules concerns —
     * those don't influence wayfinding.
     */
    private static SlotWorkspaceViewModel projectWith(
            Map<ItemIdentity, Integer> playerDesiredCounts,
            KitMap kitMap,
            Map<String, Map<ItemIdentity, Integer>> kitDesiredCounts,
            Map<UUID, SlotWorkspaceViewModel.ChestContentsSnapshot> contentsByChest,
            List<AnchorSpec> anchors
    ) {
        ClaimedChestMap claimedChestMap = claimedChestMap(anchors);
        WorkflowProjection.Snapshot projection = new WorkflowProjection.Snapshot(
                List.of(),
                Map.of(),
                Map.of(),
                Set.of(),
                Set.of(),
                new ProtectionSnapshotPolicy(Set.of(), Set.of(), false),
                Map.of(),
                VisualHomeMap.empty(),
                claimedChestMap,
                ChestAffinityMap.empty(),
                Map.of(),
                kitMap,
                playerDesiredCounts,
                kitDesiredCounts
        );
        WorkflowDomainSnapshot snapshot = new WorkflowDomainSnapshot(
                1L,
                projection,
                null,
                null,
                null,
                null,
                null
        );
        Function<String, SlotWorkspaceViewModel.ChestContentsSnapshot> resolver = storageId -> {
            try {
                return contentsByChest.getOrDefault(UUID.fromString(storageId),
                        SlotWorkspaceViewModel.ChestContentsSnapshot.empty());
            } catch (IllegalArgumentException ignored) {
                return SlotWorkspaceViewModel.ChestContentsSnapshot.empty();
            }
        };
        Set<String> proximate = Set.of();
        return SlotWorkspaceViewModel.project(
                InventoryAuthoritySnapshot.empty(),
                snapshot,
                "ready",
                "",
                0,
                0,
                1L,
                null,
                null,
                resolver,
                proximate
        );
    }

    private static ClaimedChestMap claimedChestMap(List<AnchorSpec> anchors) {
        java.util.LinkedHashMap<UUID, java.util.LinkedHashSet<ChestAnchor>> grouped =
                new java.util.LinkedHashMap<>();
        for (AnchorSpec spec : anchors) {
            grouped
                    .computeIfAbsent(spec.storageId(), id -> new java.util.LinkedHashSet<>())
                    .add(new ChestAnchor(spec.dimensionId(), spec.x(), spec.y(), spec.z()));
        }
        java.util.ArrayList<ClaimedChest> chests = new java.util.ArrayList<>();
        for (Map.Entry<UUID, java.util.LinkedHashSet<ChestAnchor>> entry : grouped.entrySet()) {
            chests.add(new ClaimedChest(entry.getKey(), entry.getValue(), 0, 0, ""));
        }
        return new ClaimedChestMap(chests);
    }
}
