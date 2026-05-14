package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkspaceSearchQueryTest {
    @Test
    void normalizationStripsSlashTrimsAndCapsLength() {
        String longQuery = "/" + "a".repeat(80);

        String normalized = WorkspaceSearchQuery.normalized(longQuery);

        assertTrue(normalized.length() == WorkspaceSearchQuery.MAX_QUERY_LENGTH);
        assertTrue(normalized.chars().allMatch(ch -> ch == 'a'));
        assertTrue(WorkspaceSearchQuery.normalized(" /Stone ").equals("stone"));
    }

    @Test
    void itemMatchUsesNameIdentityAndIslandFields() {
        SlotWorkspaceViewModel.AtlasItem item = item("minecraft:oak_log", "Oak Log", "materials");
        SlotWorkspaceViewModel.AtlasIsland island = new SlotWorkspaceViewModel.AtlasIsland(
                "materials",
                "Ores & Raw Stock",
                VisualAtlasIslandKind.PLAYER,
                0,
                0,
                0,
                1,
                0);

        assertTrue(WorkspaceSearchQuery.matchesItem("oak", item, island));
        assertTrue(WorkspaceSearchQuery.matchesItem("raw", item, island));
        assertFalse(WorkspaceSearchQuery.matchesItem("diamond", item, island));
    }

    @Test
    void itemMatchUsesDisplayStackHoverName() {
        ItemStack stack = new ItemStack("beneath:cursecoal", 1, 64)
                .setHoverName(Component.literal("Anthracite"));
        SlotWorkspaceViewModel.AtlasItem item = item(
                ItemIdentity.of("beneath:cursecoal"),
                stack,
                "Cursecoal",
                "fuel");

        assertTrue(WorkspaceSearchQuery.matchesItem("anth", item, null));
        assertTrue(WorkspaceSearchQuery.matchesItem("coal", item, null));
    }

    @Test
    void identityStackMatchUsesSameNormalization() {
        assertTrue(WorkspaceSearchQuery.matchesIdentityStack(
                "/stone",
                ItemIdentity.of("minecraft:stone"),
                new ItemStack("minecraft:stone", 1, 64)));
        assertFalse(WorkspaceSearchQuery.matchesIdentityStack(
                "copper",
                ItemIdentity.of("minecraft:stone"),
                new ItemStack("minecraft:stone", 1, 64)));
    }

    private static SlotWorkspaceViewModel.AtlasItem item(String itemId, String name, String islandId) {
        return item(ItemIdentity.of(itemId), new ItemStack(itemId, 1, 64), name, islandId);
    }

    private static SlotWorkspaceViewModel.AtlasItem item(
            ItemIdentity identity,
            ItemStack displayStack,
            String name,
            String islandId
    ) {
        return new SlotWorkspaceViewModel.AtlasItem(
                SlotWorkspaceViewModel.IdentityRef.from(identity),
                displayStack,
                name,
                1,
                0,
                islandId,
                false,
                false,
                true,
                false,
                0,
                List.of(),
                List.of(),
                List.of(),
                false,
                0,
                0,
                false,
                0,
                false,
                "",
                -1,
                0);
    }
}
