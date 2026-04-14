package dev.imagio.slot.client.screen;

import dev.imagio.slot.client.category.SlotCategory;
import dev.imagio.slot.projection.InventoryViewData;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InventoryRailSupportTest {
    private static final String ALL = "__all__";

    @Test
    void buildsTargetsFromSections() {
        List<InventoryRailSupport.Target> targets = InventoryRailSupport.buildTargets(
                ALL,
                "All",
                List.of(
                        InventoryViewData.Section.recent("Recent", 0),
                        InventoryViewData.Section.collection("tools", "Tools", 1),
                        InventoryViewData.Section.modBucket("minecraft", "Minecraft", 2),
                        InventoryViewData.Section.category(SlotCategory.BUILDING, 3)
                )
        );

        assertEquals(InventoryRailSupport.Kind.ALL, targets.get(0).kind());
        assertEquals(InventoryRailSupport.Kind.RECENT, targets.get(1).kind());
        assertEquals(InventoryRailSupport.Kind.COLLECTION, targets.get(2).kind());
        assertEquals(InventoryRailSupport.Kind.MOD_BUCKET, targets.get(3).kind());
        assertEquals(InventoryRailSupport.Kind.CATEGORY, targets.get(4).kind());
    }

    @Test
    void resolvesInaccessibleTargetToNearestAccessibleNeighbor() {
        List<InventoryRailSupport.Target> targets = List.of(
                new InventoryRailSupport.Target(ALL, "All", InventoryRailSupport.Kind.ALL),
                new InventoryRailSupport.Target("a", "A", InventoryRailSupport.Kind.CATEGORY),
                new InventoryRailSupport.Target("b", "B", InventoryRailSupport.Kind.CATEGORY),
                new InventoryRailSupport.Target("c", "C", InventoryRailSupport.Kind.CATEGORY)
        );
        Set<String> accessible = Set.of("c");

        assertEquals("c", InventoryRailSupport.resolveVisibleTargetId(ALL, targets, "b", accessible::contains));
        assertEquals("missing", InventoryRailSupport.resolveVisibleTargetId(ALL, targets, "missing", accessible::contains));
        assertEquals(ALL, InventoryRailSupport.resolveVisibleTargetId(ALL, targets, ALL, accessible::contains));
    }

    @Test
    void contentHeightAddsGroupHeadersOnlyWhenKindChanges() {
        List<InventoryRailSupport.Target> targets = List.of(
                new InventoryRailSupport.Target(ALL, "All", InventoryRailSupport.Kind.ALL),
                new InventoryRailSupport.Target("a", "A", InventoryRailSupport.Kind.COLLECTION),
                new InventoryRailSupport.Target("b", "B", InventoryRailSupport.Kind.COLLECTION),
                new InventoryRailSupport.Target("c", "C", InventoryRailSupport.Kind.CATEGORY)
        );

        assertEquals(4 * 16 + 2 * 12, InventoryRailSupport.contentHeight(targets, 16, 12));
    }

    @Test
    void hitTargetUsesSameGroupedRailGeometryAsRendering() {
        List<InventoryRailSupport.Target> targets = List.of(
                new InventoryRailSupport.Target(ALL, "All", InventoryRailSupport.Kind.ALL),
                new InventoryRailSupport.Target("a", "A", InventoryRailSupport.Kind.COLLECTION),
                new InventoryRailSupport.Target("b", "B", InventoryRailSupport.Kind.COLLECTION)
        );

        assertEquals(ALL, InventoryRailSupport.hitTarget(targets, 10, 11, 0, 100, 10, 0, 16, 12).id());
        assertEquals("a", InventoryRailSupport.hitTarget(targets, 10, 39, 0, 100, 10, 0, 16, 12).id());
        assertEquals("b", InventoryRailSupport.hitTarget(targets, 10, 55, 0, 100, 10, 0, 16, 12).id());
    }
}
