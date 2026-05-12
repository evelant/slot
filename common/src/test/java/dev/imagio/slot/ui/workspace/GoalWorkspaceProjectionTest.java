package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.goal.GoalChoiceKeys;
import dev.imagio.slot.inventory.goal.GoalChoiceResolution;
import dev.imagio.slot.inventory.goal.GoalDescriptor;
import dev.imagio.slot.inventory.goal.GoalIngredientDescriptor;
import dev.imagio.slot.inventory.goal.GoalRecipeDescriptor;
import dev.imagio.slot.inventory.goal.GoalRecipeDefaults;
import dev.imagio.slot.inventory.goal.GoalStackDescriptor;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceAtlasLayout;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GoalWorkspaceProjectionTest {
    private static final ItemIdentity BRICK = ItemIdentity.of("minecraft:brick");
    private static final ItemIdentity WATER = ItemIdentity.of("minecraft:water_bucket");
    private static final ItemIdentity PRESS = ItemIdentity.of("firmalife:wood/barrel_press/blackwood");
    private static final ItemIdentity GLUE = ItemIdentity.of("tfg:glue");
    private static final ItemIdentity SLIME = ItemIdentity.of("minecraft:slime_ball");
    private static final ItemIdentity LIMEWATER = ItemIdentity.of("tfc:limewater_bucket");
    private static final ItemIdentity BONEMEAL = ItemIdentity.of("minecraft:bone_meal");
    private static final ItemIdentity STEEL_SAW = ItemIdentity.of("tfc:metal/saw/steel");
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
    void aggregatesRepeatedRequirementsAndKeepsNormalSections() {
        GoalWorkspaceProjection projection = GoalWorkspaceProjection.fromGoal(sourceView(), duplicateGoal(), 1);

        assertNotNull(projection);
        assertFalse(projection.islands().stream()
                .anyMatch(island -> "goal.requirements".equals(island.islandId())));
        assertNotNull(island(projection, BUILDING));
        assertNotNull(island(projection, SlotWorkspaceAtlasLayout.ISLAND_MISC));

        SlotWorkspaceViewModel.AtlasItem brick = projection.atlasItem(SlotWorkspaceViewModel.IdentityRef.from(BRICK));
        assertNotNull(brick);
        assertEquals(BUILDING, brick.islandId());
        assertEquals(1, brick.totalCount());
        assertEquals(0, brick.desiredCount());
        assertEquals(5, brick.wantedCount());

        long brickCards = projection.atlasItems().stream()
                .filter(item -> SlotWorkspaceViewModel.IdentityRef.from(BRICK).equals(item.identity()))
                .count();
        assertEquals(1, brickCards);

        SlotWorkspaceViewModel.AtlasItem water = projection.atlasItem(SlotWorkspaceViewModel.IdentityRef.from(WATER));
        assertNotNull(water);
        assertEquals(SlotWorkspaceAtlasLayout.ISLAND_MISC, water.islandId());
        assertEquals(0, water.desiredCount());
        assertEquals(1, water.wantedCount());
    }

    @Test
    void unresolvedChoiceCardRendersPlaceholderWhenAlternativesHaveNoDisplayStack() {
        SlotWorkspaceViewModel.setGhostStackResolver(id -> "minecraft:knowledge_book".equals(id)
                ? new ItemStack(id, 1, 64)
                : ItemStack.EMPTY);

        GoalWorkspaceProjection projection = GoalWorkspaceProjection.fromGoal(
                SlotWorkspaceViewModel.empty(),
                unresolvedChoiceGoal(),
                1);

        SlotWorkspaceViewModel.AtlasItem choice = projection.atlasItems().stream()
                .filter(projection::choiceCard)
                .findFirst()
                .orElseThrow();
        assertEquals("Any impossible binder", choice.name());
        assertEquals("minecraft:knowledge_book", choice.displayStack().itemId());
        assertEquals("Choice: Any impossible binder", choice.displayStack().getHoverName().getString());
        assertTrue(projection.suppressVanillaTooltip(choice));
    }

    @Test
    void concreteRequirementRendersPlaceholderWhenDescriptorCannotResolveStack() {
        SlotWorkspaceViewModel.setGhostStackResolver(id -> "minecraft:knowledge_book".equals(id)
                ? new ItemStack(id, 1, 64)
                : ItemStack.EMPTY);

        GoalWorkspaceProjection projection = GoalWorkspaceProjection.fromGoal(
                SlotWorkspaceViewModel.empty(),
                unresolvedConcreteGoal(),
                1);

        ItemIdentity missing = ItemIdentity.of("slot:missing_concrete");
        SlotWorkspaceViewModel.AtlasItem item = projection.atlasItem(SlotWorkspaceViewModel.IdentityRef.from(missing));
        assertNotNull(item);
        assertEquals("Missing Concrete", item.name());
        assertEquals(0, item.desiredCount());
        assertEquals(1, item.wantedCount());
        assertEquals("minecraft:knowledge_book", item.displayStack().itemId());
        assertEquals("Missing: Missing Concrete", item.displayStack().getHoverName().getString());
        assertTrue(projection.suppressVanillaTooltip(item));
    }

    @Test
    void syntheticLimewaterRequirementUsesBarrelDisplayFallback() {
        SlotWorkspaceViewModel.setGhostStackResolver(id -> "minecraft:barrel".equals(id)
                ? new ItemStack(id, 1, 64)
                : ItemStack.EMPTY);
        ItemIdentity output = ItemIdentity.of("slot:glue");
        ItemIdentity limewater = ItemIdentity.of("slot:emi/tfc/limewater");
        GoalStackDescriptor outputStack = new GoalStackDescriptor(output, "Glue", 1);

        GoalWorkspaceProjection projection = GoalWorkspaceProjection.fromGoal(
                SlotWorkspaceViewModel.empty(),
                new GoalDescriptor(
                        "goal:glue",
                        "Glue",
                        List.of(outputStack),
                        1,
                        "slot:recipe/glue",
                        "slot:test",
                        List.of(new GoalRecipeDescriptor(
                                "slot:recipe/glue",
                                "slot:test",
                                true,
                                List.of(outputStack),
                                List.of(GoalIngredientDescriptor.concrete(
                                        "limewater",
                                        new GoalStackDescriptor(limewater, "Limewater", 1),
                                        1)),
                                List.of(),
                                List.of()))),
                1);

        SlotWorkspaceViewModel.AtlasItem item =
                projection.atlasItem(SlotWorkspaceViewModel.IdentityRef.from(limewater));
        assertNotNull(item);
        assertEquals("minecraft:barrel", item.displayStack().itemId());
        assertEquals("Limewater", item.displayStack().getHoverName().getString());
    }

    @Test
    void unresolvedProducerChoiceUsesExistingRequirementCard() {
        GoalWorkspaceProjection projection = GoalWorkspaceProjection.fromGoal(
                SlotWorkspaceViewModel.empty(),
                producerChoiceGoal(),
                1);

        SlotWorkspaceViewModel.IdentityRef glueRef = SlotWorkspaceViewModel.IdentityRef.from(GLUE);
        SlotWorkspaceViewModel.AtlasItem glue = projection.atlasItem(glueRef);
        assertNotNull(glue);
        assertTrue(projection.choiceInvolved(glue));
        assertFalse(projection.choiceCard(glue));
        assertEquals(1, projection.atlasItems().stream()
                .filter(item -> glueRef.equals(item.identity()))
                .count());
        assertEquals(1, projection.atlasItems().size());
    }

    @Test
    void manualProducerRecipeChoiceClearsQuestionIconAndExpandsRoute() {
        String producerChoice = GoalChoiceKeys.producerChoiceGroupId("slot:recipe/press", "glue", GLUE);
        GoalWorkspaceProjection projection = GoalWorkspaceProjection.fromGoal(
                SlotWorkspaceViewModel.empty(),
                producerChoiceGoal(),
                1,
                GoalChoiceResolution.empty().withRecipeChoice(producerChoice, "slot:recipe/glue_from_barrel"));

        SlotWorkspaceViewModel.AtlasItem glue = projection.atlasItem(SlotWorkspaceViewModel.IdentityRef.from(GLUE));
        assertNotNull(glue);
        assertFalse(projection.choiceInvolved(glue));
        assertTrue(projection.hasManualChoice(glue));
        assertNotNull(projection.atlasItem(SlotWorkspaceViewModel.IdentityRef.from(LIMEWATER)));
        assertNotNull(projection.atlasItem(SlotWorkspaceViewModel.IdentityRef.from(BONEMEAL)));
        assertEquals(1, projection.atlasItems().stream()
                .filter(item -> SlotWorkspaceViewModel.IdentityRef.from(GLUE).equals(item.identity()))
                .count());
    }

    @Test
    void rememberedProducerRecipeDefaultClearsQuestionIconAndKeepsChoiceControls() {
        GoalWorkspaceProjection projection = GoalWorkspaceProjection.fromGoal(
                SlotWorkspaceViewModel.empty(),
                producerChoiceGoal(),
                1,
                GoalChoiceResolution.empty(),
                GoalRecipeDefaults.empty().withRecipeChoice(GLUE, "slot:recipe/glue_from_barrel"));

        SlotWorkspaceViewModel.AtlasItem glue = projection.atlasItem(SlotWorkspaceViewModel.IdentityRef.from(GLUE));
        assertNotNull(glue);
        assertFalse(projection.choiceInvolved(glue));
        assertFalse(projection.hasManualChoice(glue));
        assertTrue(projection.hasRecipeDefaultChoice(glue));
        assertTrue(projection.hasChoiceControls(glue));
        assertNotNull(projection.atlasItem(SlotWorkspaceViewModel.IdentityRef.from(LIMEWATER)));
        assertNotNull(projection.atlasItem(SlotWorkspaceViewModel.IdentityRef.from(BONEMEAL)));
    }

    @Test
    void serverRecipeDefaultFromViewModelFeedsActiveGoalProjection() {
        GoalWorkspaceClientState.addOrActivate(producerChoiceGoal());
        SlotWorkspaceViewModel source = viewWithRecipeDefaults(
                GoalRecipeDefaults.empty().withRecipeChoice(GLUE, "slot:recipe/glue_from_barrel"));

        GoalWorkspaceProjection projection = GoalWorkspaceProjection.fromGoal(
                source,
                GoalWorkspaceClientState.activeGoal());

        SlotWorkspaceViewModel.AtlasItem glue = projection.atlasItem(SlotWorkspaceViewModel.IdentityRef.from(GLUE));
        assertNotNull(glue);
        assertFalse(projection.choiceInvolved(glue));
        assertTrue(projection.hasRecipeDefaultChoice(glue));
        assertNotNull(projection.atlasItem(SlotWorkspaceViewModel.IdentityRef.from(LIMEWATER)));
        assertNotNull(projection.atlasItem(SlotWorkspaceViewModel.IdentityRef.from(BONEMEAL)));
    }

    @Test
    void trackedStorageChoiceAutoResolvesWithoutSearchMaterializingCard() {
        GoalWorkspaceProjection projection = GoalWorkspaceProjection.fromGoal(
                viewWithTrackedChestSaw(false),
                sawChoiceGoal(),
                1);

        SlotWorkspaceViewModel.AtlasItem saw = projection.atlasItem(SlotWorkspaceViewModel.IdentityRef.from(STEEL_SAW));
        assertNotNull(saw);
        assertTrue(projection.choiceInvolved(saw));
        assertFalse(projection.choiceCard(saw));
        assertEquals(0, saw.wantedCount());
        assertEquals(0, projection.atlasItems().stream()
                .filter(projection::choiceCard)
                .count());
    }

    @Test
    void movableEquivalentRequirementsMergeIntoOneCard() {
        ItemIdentity saw = ItemIdentity.of("slot:wrought_iron_saw");
        ItemIdentity exactSaw = ItemIdentity.exact("slot:wrought_iron_saw", "damage=1");
        ItemIdentity output = ItemIdentity.of("slot:saw_output");
        GoalStackDescriptor outputStack = new GoalStackDescriptor(output, "Saw Output", 1);
        GoalWorkspaceProjection projection = GoalWorkspaceProjection.fromGoal(
                SlotWorkspaceViewModel.empty(),
                new GoalDescriptor(
                        "goal:saw",
                        "Saw Output",
                        List.of(outputStack),
                        1,
                        "slot:recipe/saw",
                        "slot:test",
                        List.of(new GoalRecipeDescriptor(
                                "slot:recipe/saw",
                                "slot:test",
                                true,
                                List.of(outputStack),
                                List.of(
                                        GoalIngredientDescriptor.concrete(
                                                "saw_a",
                                                new GoalStackDescriptor(saw, "Wrought Iron Saw", 1),
                                                1),
                                        GoalIngredientDescriptor.concrete(
                                                "saw_b",
                                                new GoalStackDescriptor(exactSaw, "Wrought Iron Saw", 1),
                                                1)),
                                List.of(),
                                List.of()))),
                1);

        assertEquals(1, projection.atlasItems().stream()
                .filter(item -> "slot:wrought_iron_saw".equals(item.identity().itemId()))
                .count());
        SlotWorkspaceViewModel.AtlasItem item = projection.atlasItem(SlotWorkspaceViewModel.IdentityRef.from(saw));
        assertNotNull(item);
        assertEquals(2, item.wantedCount());
    }

    private static SlotWorkspaceViewModel sourceView() {
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
                List.of(new SlotWorkspaceViewModel.AtlasItem(
                        SlotWorkspaceViewModel.IdentityRef.from(BRICK),
                        new ItemStack("minecraft:brick", 1, 64),
                        "Brick",
                        1,
                        0,
                        BUILDING,
                        false,
                        true,
                        true,
                        List.of())),
                List.of(),
                List.of(),
                List.of(),
                SlotWorkspaceViewModel.OffhandSlot.empty(),
                List.of());
    }

    private static SlotWorkspaceViewModel viewWithRecipeDefaults(GoalRecipeDefaults defaults) {
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
                SlotWorkspaceAtlasLayout.baseIslands(dev.imagio.slot.workflow.domain.VisualHomeMap.empty()),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                SlotWorkspaceViewModel.emptyHotbar(),
                SlotWorkspaceViewModel.OffhandSlot.empty(),
                List.of(),
                SlotWorkspaceViewModel.LootChestPanel.empty(),
                List.of(),
                java.util.Set.of(),
                List.of(),
                SlotWorkspaceViewModel.ActiveChestPanel.empty(),
                defaults
        );
    }

    private static SlotWorkspaceViewModel viewWithTrackedChestSaw(boolean proximate) {
        ItemStack sawStack = new ItemStack("tfc:metal/saw/steel", "damage=7", 1, 1);
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
                SlotWorkspaceAtlasLayout.baseIslands(dev.imagio.slot.workflow.domain.VisualHomeMap.empty()),
                List.of(),
                List.of(),
                List.of(new SlotWorkspaceViewModel.ChestChip(
                        "storage:saw",
                        "minecraft:overworld",
                        "Tool Chest",
                        1,
                        27,
                        1,
                        proximate,
                        0,
                        4,
                        64,
                        4,
                        "",
                        List.of(new SlotWorkspaceViewModel.ChestContentSummary(
                                "tfc:metal/saw/steel",
                                "damage=7",
                                "Steel Saw",
                                sawStack,
                                1)))),
                List.of(),
                SlotWorkspaceViewModel.emptyHotbar(),
                SlotWorkspaceViewModel.OffhandSlot.empty(),
                List.of(),
                SlotWorkspaceViewModel.LootChestPanel.empty(),
                List.of(),
                java.util.Set.of(),
                List.of(),
                SlotWorkspaceViewModel.ActiveChestPanel.empty()
        );
    }

    private static SlotWorkspaceViewModel.AtlasIsland island(
            GoalWorkspaceProjection projection,
            String islandId
    ) {
        return projection.islands().stream()
                .filter(island -> islandId.equals(island.islandId()))
                .findFirst()
                .orElse(null);
    }

    private static GoalDescriptor duplicateGoal() {
        ItemIdentity furnace = ItemIdentity.of("minecraft:furnace");
        GoalStackDescriptor output = new GoalStackDescriptor(furnace, "Furnace", 1);
        return new GoalDescriptor(
                "goal:furnace",
                "Furnace",
                List.of(output),
                1,
                "slot:recipe/furnace",
                "slot:test",
                List.of(new GoalRecipeDescriptor(
                        "slot:recipe/furnace",
                        "slot:test",
                        true,
                        List.of(output),
                        List.of(
                                GoalIngredientDescriptor.concrete("brick_a", new GoalStackDescriptor(BRICK, "Brick", 1), 2),
                                GoalIngredientDescriptor.concrete("brick_b", new GoalStackDescriptor(BRICK, "Brick", 1), 3),
                                GoalIngredientDescriptor.concrete("water", new GoalStackDescriptor(WATER, "Water Bucket", 1), 1)
                        ),
                        List.of(),
                        List.of()
                ))
        );
    }

    private static GoalDescriptor sawChoiceGoal() {
        ItemIdentity planks = ItemIdentity.of("tfc:wood/planks/blackwood");
        GoalStackDescriptor output = new GoalStackDescriptor(planks, "Blackwood Planks", 1);
        return new GoalDescriptor(
                "goal:planks",
                "Blackwood Planks",
                List.of(output),
                1,
                "slot:recipe/planks",
                "slot:test",
                List.of(new GoalRecipeDescriptor(
                        "slot:recipe/planks",
                        "slot:test",
                        true,
                        List.of(output),
                        List.of(new GoalIngredientDescriptor(
                                "saw",
                                "Any Saw",
                                1,
                                1.0D,
                                "",
                                List.of(new GoalStackDescriptor(STEEL_SAW, "Steel Saw", 1)),
                                true,
                                false,
                                "#item:forge:tools/saws",
                                List.of("ingredient_not_consumed"))),
                        List.of(),
                        List.of()
                ))
        );
    }

    private static GoalDescriptor unresolvedChoiceGoal() {
        ItemIdentity output = ItemIdentity.of("slot:choice_output");
        GoalStackDescriptor outputStack = new GoalStackDescriptor(output, "Choice Output", 1);
        return new GoalDescriptor(
                "goal:choice",
                "Choice Output",
                List.of(outputStack),
                1,
                "slot:recipe/choice",
                "slot:test",
                List.of(new GoalRecipeDescriptor(
                        "slot:recipe/choice",
                        "slot:test",
                        true,
                        List.of(outputStack),
                        List.of(GoalIngredientDescriptor.choice(
                                "binder",
                                "Any impossible binder",
                                1,
                                "#slot:missing_binders",
                                List.of(new GoalStackDescriptor(
                                        ItemIdentity.of("slot:missing_binder"),
                                        "Missing Binder",
                                        1))
                        )),
                        List.of(),
                        List.of()
                ))
        );
    }

    private static GoalDescriptor unresolvedConcreteGoal() {
        ItemIdentity output = ItemIdentity.of("slot:concrete_output");
        ItemIdentity missing = ItemIdentity.of("slot:missing_concrete");
        GoalStackDescriptor outputStack = new GoalStackDescriptor(output, "Concrete Output", 1);
        return new GoalDescriptor(
                "goal:concrete",
                "Concrete Output",
                List.of(outputStack),
                1,
                "slot:recipe/concrete",
                "slot:test",
                List.of(new GoalRecipeDescriptor(
                        "slot:recipe/concrete",
                        "slot:test",
                        true,
                        List.of(outputStack),
                        List.of(GoalIngredientDescriptor.concrete(
                                "missing",
                                new GoalStackDescriptor(missing, "Missing Concrete", 1),
                                1)),
                        List.of(),
                        List.of()
                ))
        );
    }

    private static GoalDescriptor producerChoiceGoal() {
        GoalStackDescriptor outputStack = new GoalStackDescriptor(PRESS, "Blackwood Barrel Press", 1);
        return new GoalDescriptor(
                "goal:press",
                "Blackwood Barrel Press",
                List.of(outputStack),
                1,
                "slot:recipe/press",
                "slot:test",
                List.of(
                        new GoalRecipeDescriptor(
                                "slot:recipe/press",
                                "slot:test",
                                true,
                                List.of(outputStack),
                                List.of(GoalIngredientDescriptor.concrete(
                                        "glue",
                                        new GoalStackDescriptor(GLUE, "Glue", 1),
                                        1)),
                                List.of(),
                                List.of()),
                        new GoalRecipeDescriptor(
                                "slot:recipe/glue_from_slime",
                                "slot:test",
                                true,
                                List.of(new GoalStackDescriptor(GLUE, "Glue", 1)),
                                List.of(GoalIngredientDescriptor.concrete(
                                        "slime",
                                        new GoalStackDescriptor(SLIME, "Slimeball", 1),
                                        1)),
                                List.of(),
                                List.of()),
                        new GoalRecipeDescriptor(
                                "slot:recipe/glue_from_barrel",
                                "slot:test",
                                true,
                                List.of(new GoalStackDescriptor(GLUE, "Glue", 1)),
                                List.of(
                                        GoalIngredientDescriptor.concrete(
                                                "limewater",
                                                new GoalStackDescriptor(LIMEWATER, "Limewater", 1),
                                                1),
                                        GoalIngredientDescriptor.concrete(
                                                "bonemeal",
                                                new GoalStackDescriptor(BONEMEAL, "Bone Meal", 1),
                                                1)),
                                List.of(),
                                List.of())
                )
        );
    }
}
