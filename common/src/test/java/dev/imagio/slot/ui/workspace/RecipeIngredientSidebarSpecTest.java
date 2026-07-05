package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceAtlasLayout;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecipeIngredientSidebarSpecTest {
    private static final ItemIdentity BRICK = ItemIdentity.of("minecraft:brick");
    private static final ItemIdentity OAK = ItemIdentity.of("minecraft:oak_planks");
    private static final ItemIdentity SPRUCE = ItemIdentity.of("minecraft:spruce_planks");
    private static final String BUILDING = "building";

    @BeforeEach
    void installDisplayStackResolver() {
        SlotWorkspaceViewModel.setGhostStackResolver(id -> new ItemStack(id, 1, 64));
    }

    @AfterEach
    void resetDisplayStackResolver() {
        SlotWorkspaceViewModel.setGhostStackResolver(null);
    }

    @Test
    void missingIngredientUsesCraftTargetState() {
        RecipeIngredientSidebarSpec.Projection projection = spec(List.of(ingredient(
                "brick",
                "Brick",
                4,
                alternative(BRICK, "Brick", 4)))).project(SlotWorkspaceViewModel.empty());

        SlotWorkspaceViewModel.AtlasItem brick = projection.atlasItem(SlotWorkspaceViewModel.IdentityRef.from(BRICK));
        assertNotNull(brick);
        assertEquals(SlotWorkspaceAtlasLayout.ISLAND_MISC, brick.islandId());
        assertEquals(0, brick.totalCount());
        assertEquals(4, brick.desiredCount());
        assertTrue(brick.ghost());
        assertEquals("minecraft:brick", brick.displayStack().itemId());
        assertEquals(4, projection.ingredient(brick).requiredCount());
    }

    @Test
    void presentIngredientKeepsHomeSectionAndRecipeRequirement() {
        RecipeIngredientSidebarSpec.Projection projection = spec(List.of(ingredient(
                "brick",
                "Brick",
                4,
                alternative(BRICK, "Brick", 4)))).project(sourceView());

        SlotWorkspaceViewModel.AtlasItem brick = projection.atlasItem(SlotWorkspaceViewModel.IdentityRef.from(BRICK));
        assertNotNull(brick);
        assertEquals(BUILDING, brick.islandId());
        assertEquals(2, brick.totalCount());
        assertEquals(4, brick.desiredCount());
        assertTrue(brick.carried());
    }

    @Test
    void alternativeIngredientShowsOnlyVisibleAlternative() {
        RecipeIngredientSidebarSpec.Projection projection = spec(List.of(ingredient(
                "planks",
                "Planks",
                1,
                alternative(SPRUCE, "Spruce Planks", 1),
                alternative(OAK, "Oak Planks", 1)))).project(sourceViewWithOak());

        assertNotNull(projection.atlasItem(SlotWorkspaceViewModel.IdentityRef.from(OAK)));
        assertNull(projection.atlasItem(SlotWorkspaceViewModel.IdentityRef.from(SPRUCE)));
        assertEquals(1, projection.atlasItems().size());
    }

    @Test
    void storedOnlyIngredientCountsAsPresentWhenProjectionIncludesElsewhereCard() {
        RecipeIngredientSidebarSpec.Projection projection = spec(List.of(ingredient(
                "brick",
                "Brick",
                4,
                alternative(BRICK, "Brick", 4)))).project(storedOnlySourceView());

        SlotWorkspaceViewModel.AtlasItem brick = projection.atlasItem(SlotWorkspaceViewModel.IdentityRef.from(BRICK));
        assertNotNull(brick);
        assertEquals(BUILDING, brick.islandId());
        assertEquals(4, brick.totalCount());
        assertEquals(4, brick.desiredCount());
        assertTrue(brick.ghost());
        assertFalse(brick.carried());
        assertEquals(1, brick.elsewhere().size());
        assertEquals(4, projection.ingredient(brick).requiredCount());
    }

    @Test
    void remoteDetailIdentitiesListsVisibleRecipeAlternatives() {
        RecipeIngredientSidebarSpec spec = spec(List.of(ingredient(
                "planks",
                "Planks",
                1,
                alternative(SPRUCE, "Spruce Planks", 1),
                alternative(OAK, "Oak Planks", 1))));

        assertEquals(Set.of(OAK, SPRUCE), spec.remoteDetailIdentities());
        assertFalse(spec.remoteDetailIdentityPayload().isBlank());
        assertEquals(Set.of(), RecipeIngredientSidebarSpec.empty().remoteDetailIdentities());
    }

    @Test
    void missingIngredientPreservesExistingWantedTarget() {
        RecipeIngredientSidebarSpec.Projection projection = spec(List.of(ingredient(
                "brick",
                "Brick",
                4,
                alternative(BRICK, "Brick", 4)))).project(sourceView(List.of(new SlotWorkspaceViewModel.AtlasItem(
                        SlotWorkspaceViewModel.IdentityRef.from(BRICK),
                        new ItemStack("minecraft:brick", 1, 64),
                        "Brick",
                        0,
                        0,
                        BUILDING,
                        false,
                        false,
                        false,
                        true,
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
                        6,
                        "",
                        -1,
                        0
                ))));

        SlotWorkspaceViewModel.AtlasItem brick = projection.atlasItem(SlotWorkspaceViewModel.IdentityRef.from(BRICK));
        assertNotNull(brick);
        assertEquals(4, brick.desiredCount());
        assertEquals(6, brick.wantedCount());
        assertTrue(brick.ghost());
    }

    private static RecipeIngredientSidebarSpec spec(List<RecipeIngredientSidebarSpec.Ingredient> ingredients) {
        return new RecipeIngredientSidebarSpec("emi:test", "EMI recipe ingredients", ingredients);
    }

    private static RecipeIngredientSidebarSpec.Ingredient ingredient(
            String id,
            String label,
            int count,
            RecipeIngredientSidebarSpec.Alternative... alternatives
    ) {
        return new RecipeIngredientSidebarSpec.Ingredient(id, label, count, List.of(alternatives));
    }

    private static RecipeIngredientSidebarSpec.Alternative alternative(ItemIdentity identity, String label, int count) {
        return new RecipeIngredientSidebarSpec.Alternative(
                identity,
                label,
                count,
                new ItemStack(identity.itemId(), count, 64));
    }

    private static SlotWorkspaceViewModel sourceView() {
        return sourceView(List.of(new SlotWorkspaceViewModel.AtlasItem(
                SlotWorkspaceViewModel.IdentityRef.from(BRICK),
                new ItemStack("minecraft:brick", 2, 64),
                "Brick",
                2,
                0,
                BUILDING,
                false,
                true,
                true,
                List.of())));
    }

    private static SlotWorkspaceViewModel sourceViewWithOak() {
        return sourceView(List.of(new SlotWorkspaceViewModel.AtlasItem(
                SlotWorkspaceViewModel.IdentityRef.from(OAK),
                new ItemStack("minecraft:oak_planks", 1, 64),
                "Oak Planks",
                1,
                0,
                BUILDING,
                false,
                true,
                true,
                List.of())));
    }

    private static SlotWorkspaceViewModel storedOnlySourceView() {
        return sourceView(List.of(new SlotWorkspaceViewModel.AtlasItem(
                SlotWorkspaceViewModel.IdentityRef.from(BRICK),
                new ItemStack("minecraft:brick", 4, 64),
                "Brick",
                4,
                0,
                BUILDING,
                false,
                false,
                false,
                true,
                0,
                List.of(),
                List.of(),
                List.of(new SlotWorkspaceViewModel.ChestPresenceEntry("remote-a", "Remote Storage", 4)),
                false,
                0,
                0,
                false,
                0,
                false,
                0,
                false,
                "",
                -1,
                0)));
    }

    private static SlotWorkspaceViewModel sourceView(List<SlotWorkspaceViewModel.AtlasItem> items) {
        return new SlotWorkspaceViewModel(
                1,
                "ready",
                "",
                0,
                -1,
                2200,
                1480,
                0,
                0,
                List.of(new SlotWorkspaceViewModel.AtlasIsland(
                        BUILDING,
                        "Building",
                        VisualAtlasIslandKind.PLAYER,
                        0,
                        0,
                        0xFF446688,
                        0)),
                items,
                List.of(),
                List.of(),
                List.of(),
                SlotWorkspaceViewModel.emptyHotbar(),
                SlotWorkspaceViewModel.OffhandSlot.empty(),
                List.of());
    }
}
