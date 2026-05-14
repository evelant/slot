package dev.imagio.slot.inventory.goal;

import dev.imagio.slot.inventory.core.ItemIdentity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GoalProjectionServiceTest {
    private static final ItemIdentity COKE_OVEN = ItemIdentity.of("slot:coke_oven");
    private static final ItemIdentity COKE_BRICK_BLOCK = ItemIdentity.of("slot:coke_brick_block");
    private static final ItemIdentity COKE_BRICK = ItemIdentity.of("slot:coke_brick");
    private static final ItemIdentity SAND = ItemIdentity.of("minecraft:sand");
    private static final ItemIdentity WATER_BUCKET = ItemIdentity.of("minecraft:water_bucket");
    private static final ItemIdentity CLAY_BALL = ItemIdentity.of("minecraft:clay_ball");
    private static final ItemIdentity BRICK = ItemIdentity.of("minecraft:brick");

    private final GoalProjectionService service = new GoalProjectionService();

    @Test
    void projectsFixtureGoalWithWantedCountsGhostsAndChoiceCards() {
        GoalProjection projection = service.project(
                cokeOvenGoal(),
                GoalVisibleAuthority.fromCounts(
                        Map.of(
                                COKE_BRICK, 3,
                                SAND, 2,
                                CLAY_BALL, 2
                        ),
                        Map.of(COKE_BRICK_BLOCK, 1),
                        Map.of()
                )
        );

        assertEquals(GoalProjectionStatus.READY, projection.status());
        assertTrue(projection.diagnostics().isEmpty());
        assertEquals(5, projection.wantedCounts().get(SAND));
        assertEquals(5, projection.wantedCounts().get(WATER_BUCKET));
        assertFalse(projection.wantedCounts().containsKey(COKE_BRICK_BLOCK));
        assertFalse(projection.wantedCounts().containsKey(COKE_BRICK));

        GoalProjectionEntry block = entryFor(projection, COKE_BRICK_BLOCK);
        assertEquals(GoalProjectionEntryKind.STORAGE_GHOST, block.kind());
        assertEquals(3, block.requiredCount());
        assertEquals(1, block.storageCount());
        assertEquals(2, block.missingCount());
        assertEquals(0, block.wantedCount());

        GoalProjectionEntry sand = entryFor(projection, SAND);
        assertEquals(GoalProjectionEntryKind.REAL_CARD, sand.kind());
        assertEquals(5, sand.requiredCount());
        assertEquals(2, sand.carriedCount());
        assertEquals(5, sand.wantedCount());

        GoalProjectionEntry water = entryFor(projection, WATER_BUCKET);
        assertEquals(GoalProjectionEntryKind.MISSING_GHOST, water.kind());
        assertEquals(5, water.requiredCount());
        assertEquals(5, water.wantedCount());
        assertEquals(List.of("Coke Oven", "Coke Brick Block", "Coke Brick", "Water Bucket"), water.breadcrumbs());

        GoalProjectionEntry clay = entryFor(projection, CLAY_BALL);
        assertEquals(GoalProjectionEntryKind.REAL_CARD, clay.kind());
        assertTrue(clay.choiceIndicator());
        assertEquals("slot:recipe/coke_brick#binder", clay.choiceGroupId());

        assertEquals(1, projection.choices().size());
        GoalChoiceRequirement choice = projection.choices().get(0);
        assertEquals("slot:recipe/coke_brick#binder", choice.choiceGroupId());
        assertEquals(3, choice.unresolvedCount());
        assertEquals(2, choice.autoResolved().get(0).count());
        assertEquals(CLAY_BALL, choice.autoResolved().get(0).identity());

        GoalProjectionEntry choiceCard = projection.entries().stream()
                .filter(entry -> entry.kind() == GoalProjectionEntryKind.CHOICE_CARD)
                .findFirst()
                .orElseThrow();
        assertEquals("Any clay binder", choiceCard.label());
        assertEquals(3, choiceCard.missingCount());
    }

    @Test
    void visibleAuthorityFullySatisfyingChoiceDoesNotEmitChoiceCard() {
        GoalProjection projection = service.project(
                cokeOvenGoal(),
                GoalVisibleAuthority.fromCounts(
                        Map.of(
                                COKE_BRICK, 3,
                                SAND, 5,
                                WATER_BUCKET, 5,
                                CLAY_BALL, 5
                        ),
                        Map.of(COKE_BRICK_BLOCK, 1),
                        Map.of()
                )
        );

        assertTrue(projection.choices().isEmpty());
        assertTrue(projection.entries().stream()
                .noneMatch(entry -> entry.kind() == GoalProjectionEntryKind.CHOICE_CARD));
        GoalProjectionEntry clay = entryFor(projection, CLAY_BALL);
        assertTrue(clay.choiceIndicator());
        assertEquals(5, clay.carriedCount());
        assertEquals(0, clay.missingCount());
    }

    @Test
    void depthLimitBlocksExpansionAndReportsDiagnostic() {
        GoalProjection projection = service.project(
                chainGoal(),
                GoalVisibleAuthority.empty(),
                new GoalProjectionOptions(1)
        );

        assertEquals(GoalProjectionStatus.BLOCKED, projection.status());
        assertTrue(projection.diagnostics().stream()
                .anyMatch(diagnostic -> diagnostic.startsWith("goal_depth_limit_exceeded:")));
    }

    @Test
    void recipeLoopsBlockExpansionAndReportDiagnostic() {
        GoalProjection projection = service.project(
                loopGoal(),
                GoalVisibleAuthority.empty()
        );

        assertEquals(GoalProjectionStatus.BLOCKED, projection.status());
        assertTrue(projection.diagnostics().stream()
                .anyMatch(diagnostic -> diagnostic.startsWith("goal_recipe_loop_detected:")));
    }

    @Test
    void multipleProducerRecipesWithoutVisibleInputsStayAChoice() {
        ItemIdentity press = ItemIdentity.of("firmalife:wood/barrel_press/blackwood");
        ItemIdentity glue = ItemIdentity.of("tfg:glue");
        ItemIdentity slime = ItemIdentity.of("minecraft:slime_ball");
        ItemIdentity limewater = ItemIdentity.of("tfc:limewater_bucket");
        ItemIdentity bonemeal = ItemIdentity.of("minecraft:bone_meal");

        GoalProjection projection = service.project(
                new GoalDescriptor(
                        "goal:press",
                        "Blackwood Barrel Press",
                        List.of(stack(press, "Blackwood Barrel Press", 1)),
                        1,
                        "slot:recipe/press",
                        "slot:test",
                        List.of(
                                recipe("slot:recipe/press", stack(press, "Blackwood Barrel Press", 1),
                                        GoalIngredientDescriptor.concrete("glue", stack(glue, "Glue", 1), 1)),
                                recipe("slot:recipe/glue_from_slime", stack(glue, "Glue", 1),
                                        GoalIngredientDescriptor.concrete("slime", stack(slime, "Slimeball", 1), 1)),
                                recipe("slot:recipe/glue_from_barrel", stack(glue, "Glue", 1),
                                        GoalIngredientDescriptor.concrete("limewater", stack(limewater, "Limewater", 1), 1),
                                        GoalIngredientDescriptor.concrete("bonemeal", stack(bonemeal, "Bone Meal", 1), 1))
                        )
                ),
                GoalVisibleAuthority.empty()
        );

        GoalProjectionEntry glueEntry = entryFor(projection, glue);
        String producerChoice = GoalChoiceKeys.producerChoiceGroupId("slot:recipe/press", "glue", glue);
        assertEquals("", glueEntry.producerRecipeId());
        assertTrue(glueEntry.choiceIndicator());
        assertEquals(producerChoice, glueEntry.choiceGroupId());
        assertTrue(glueEntry.diagnostics().contains("producer_choice_required"));
        assertTrue(projection.entries().stream()
                .noneMatch(entry -> slime.equals(entry.identity()) || bonemeal.equals(entry.identity())));
        assertTrue(projection.entries().stream()
                .noneMatch(entry -> entry.kind() == GoalProjectionEntryKind.CHOICE_CARD));
        GoalChoiceRequirement choice = projection.choices().stream()
                .filter(entry -> producerChoice.equals(entry.choiceGroupId()))
                .findFirst()
                .orElseThrow();
        assertEquals("Choose recipe for Glue", choice.label());
        assertTrue(choice.diagnostics().contains("producer_choice_required"));
    }

    @Test
    void multipleProducerRecipesUseSingleRouteWithVisibleInputs() {
        ItemIdentity press = ItemIdentity.of("firmalife:wood/barrel_press/blackwood");
        ItemIdentity glue = ItemIdentity.of("tfg:glue");
        ItemIdentity slime = ItemIdentity.of("minecraft:slime_ball");
        ItemIdentity limewater = ItemIdentity.of("tfc:limewater_bucket");
        ItemIdentity bonemeal = ItemIdentity.of("minecraft:bone_meal");

        GoalProjection projection = service.project(
                new GoalDescriptor(
                        "goal:press",
                        "Blackwood Barrel Press",
                        List.of(stack(press, "Blackwood Barrel Press", 1)),
                        1,
                        "slot:recipe/press",
                        "slot:test",
                        List.of(
                                recipe("slot:recipe/press", stack(press, "Blackwood Barrel Press", 1),
                                        GoalIngredientDescriptor.concrete("glue", stack(glue, "Glue", 1), 1)),
                                recipe("slot:recipe/glue_from_slime", stack(glue, "Glue", 1),
                                        GoalIngredientDescriptor.concrete("slime", stack(slime, "Slimeball", 1), 1)),
                                recipe("slot:recipe/glue_from_barrel", stack(glue, "Glue", 1),
                                        GoalIngredientDescriptor.concrete("limewater", stack(limewater, "Limewater", 1), 1),
                                        GoalIngredientDescriptor.concrete("bonemeal", stack(bonemeal, "Bone Meal", 1), 1))
                        )
                ),
                GoalVisibleAuthority.fromCounts(Map.of(slime, 1), Map.of(), Map.of())
        );

        GoalProjectionEntry glueEntry = entryFor(projection, glue);
        assertEquals("slot:recipe/glue_from_slime", glueEntry.producerRecipeId());
        assertFalse(glueEntry.choiceIndicator());
        assertTrue(projection.entries().stream()
                .noneMatch(entry -> entry.kind() == GoalProjectionEntryKind.CHOICE_CARD));
        GoalProjectionEntry slimeEntry = entryFor(projection, slime);
        assertEquals(1, slimeEntry.carriedCount());
        assertEquals(0, slimeEntry.missingCount());
        assertTrue(projection.entries().stream().noneMatch(entry -> bonemeal.equals(entry.identity())));
    }

    @Test
    void manualProducerRecipeChoiceExpandsSelectedRouteWithoutVisibleInputs() {
        ItemIdentity press = ItemIdentity.of("firmalife:wood/barrel_press/blackwood");
        ItemIdentity glue = ItemIdentity.of("tfg:glue");
        ItemIdentity slime = ItemIdentity.of("minecraft:slime_ball");
        ItemIdentity limewater = ItemIdentity.of("tfc:limewater_bucket");
        ItemIdentity bonemeal = ItemIdentity.of("minecraft:bone_meal");

        GoalDescriptor goal = new GoalDescriptor(
                "goal:press",
                "Blackwood Barrel Press",
                List.of(stack(press, "Blackwood Barrel Press", 1)),
                1,
                "slot:recipe/press",
                "slot:test",
                List.of(
                        recipe("slot:recipe/press", stack(press, "Blackwood Barrel Press", 1),
                                GoalIngredientDescriptor.concrete("glue", stack(glue, "Glue", 1), 1)),
                        recipe("slot:recipe/glue_from_slime", stack(glue, "Glue", 1),
                                GoalIngredientDescriptor.concrete("slime", stack(slime, "Slimeball", 1), 1)),
                        recipe("slot:recipe/glue_from_barrel", stack(glue, "Glue", 1),
                                GoalIngredientDescriptor.concrete("limewater", stack(limewater, "Limewater", 1), 1),
                                GoalIngredientDescriptor.concrete("bonemeal", stack(bonemeal, "Bone Meal", 1), 1))
                )
        );

        GoalProjection projection = service.project(
                goal,
                GoalVisibleAuthority.empty(),
                GoalProjectionOptions.defaults(),
                GoalChoiceResolution.empty().withRecipeChoice(
                        GoalChoiceKeys.producerChoiceGroupId("slot:recipe/press", "glue", glue),
                        "slot:recipe/glue_from_barrel")
        );

        GoalProjectionEntry glueEntry = entryFor(projection, glue);
        String producerChoice = GoalChoiceKeys.producerChoiceGroupId("slot:recipe/press", "glue", glue);
        assertEquals("slot:recipe/glue_from_barrel", glueEntry.producerRecipeId());
        assertFalse(glueEntry.choiceIndicator());
        assertEquals(producerChoice, glueEntry.choiceGroupId());
        assertTrue(projection.entries().stream()
                .noneMatch(entry -> entry.kind() == GoalProjectionEntryKind.CHOICE_CARD));
        assertTrue(projection.entries().stream().noneMatch(entry -> slime.equals(entry.identity())));
        assertEquals(1, entryFor(projection, limewater).wantedCount());
        assertEquals(1, entryFor(projection, bonemeal).wantedCount());
    }

    @Test
    void rememberedProducerRecipeDefaultExpandsSelectedRouteWithoutVisibleInputs() {
        ItemIdentity press = ItemIdentity.of("firmalife:wood/barrel_press/blackwood");
        ItemIdentity glue = ItemIdentity.of("tfg:glue");
        ItemIdentity slime = ItemIdentity.of("minecraft:slime_ball");
        ItemIdentity limewater = ItemIdentity.of("tfc:limewater_bucket");
        ItemIdentity bonemeal = ItemIdentity.of("minecraft:bone_meal");
        GoalDescriptor goal = new GoalDescriptor(
                "goal:press",
                "Blackwood Barrel Press",
                List.of(stack(press, "Blackwood Barrel Press", 1)),
                1,
                "slot:recipe/press",
                "slot:test",
                List.of(
                        recipe("slot:recipe/press", stack(press, "Blackwood Barrel Press", 1),
                                GoalIngredientDescriptor.concrete("glue", stack(glue, "Glue", 1), 1)),
                        recipe("slot:recipe/glue_from_slime", stack(glue, "Glue", 1),
                                GoalIngredientDescriptor.concrete("slime", stack(slime, "Slimeball", 1), 1)),
                        recipe("slot:recipe/glue_from_barrel", stack(glue, "Glue", 1),
                                GoalIngredientDescriptor.concrete("limewater", stack(limewater, "Limewater", 1), 1),
                                GoalIngredientDescriptor.concrete("bonemeal", stack(bonemeal, "Bone Meal", 1), 1))
                )
        );

        GoalProjection projection = service.project(
                goal,
                GoalVisibleAuthority.empty(),
                GoalProjectionOptions.defaults(),
                GoalChoiceResolution.empty(),
                GoalRecipeDefaults.empty().withRecipeChoice(glue, "slot:recipe/glue_from_barrel")
        );

        GoalProjectionEntry glueEntry = entryFor(projection, glue);
        String producerChoice = GoalChoiceKeys.producerChoiceGroupId("slot:recipe/press", "glue", glue);
        assertEquals("slot:recipe/glue_from_barrel", glueEntry.producerRecipeId());
        assertFalse(glueEntry.choiceIndicator());
        assertEquals(producerChoice, glueEntry.choiceGroupId());
        assertTrue(projection.entries().stream().noneMatch(entry -> slime.equals(entry.identity())));
        assertEquals(1, entryFor(projection, limewater).wantedCount());
        assertEquals(1, entryFor(projection, bonemeal).wantedCount());
    }

    @Test
    void reusableToolRequirementDoesNotScaleWithRecipeBatches() {
        ItemIdentity planks = ItemIdentity.of("tfc:wood/planks/blackwood");
        ItemIdentity log = ItemIdentity.of("tfc:wood/log/blackwood");
        ItemIdentity saw = ItemIdentity.of("tfc:metal/saw/wrought_iron");

        GoalProjection projection = service.project(
                new GoalDescriptor(
                        "goal:planks",
                        "Blackwood Planks",
                        List.of(stack(planks, "Blackwood Planks", 1)),
                        10,
                        "slot:recipe/planks",
                        "slot:test",
                        List.of(recipe(
                                "slot:recipe/planks",
                                stack(planks, "Blackwood Planks", 1),
                                GoalIngredientDescriptor.concrete("log", stack(log, "Blackwood Log", 1), 1),
                                GoalIngredientDescriptor.reusable("saw", stack(saw, "Wrought Iron Saw", 1), 1)
                        ))
                ),
                GoalVisibleAuthority.fromCounts(Map.of(saw, 1), Map.of(), Map.of())
        );

        GoalProjectionEntry logEntry = entryFor(projection, log);
        assertEquals(10, logEntry.requiredCount());
        assertEquals(10, logEntry.wantedCount());

        GoalProjectionEntry sawEntry = entryFor(projection, saw);
        assertEquals(1, sawEntry.requiredCount());
        assertEquals(1, sawEntry.carriedCount());
        assertEquals(0, sawEntry.missingCount());
        assertEquals(0, sawEntry.wantedCount());
    }

    @Test
    void choiceIngredientAutoResolvesFromProximateStorage() {
        ItemIdentity planks = ItemIdentity.of("tfc:wood/planks/blackwood");
        ItemIdentity log = ItemIdentity.of("tfc:wood/log/blackwood");
        ItemIdentity wroughtIronSaw = ItemIdentity.of("tfc:metal/saw/wrought_iron");
        ItemIdentity steelSaw = ItemIdentity.of("tfc:metal/saw/steel");
        ItemIdentity exactStoredSteelSaw = ItemIdentity.exact("tfc:metal/saw/steel", "damage=12");

        GoalProjection projection = service.project(
                new GoalDescriptor(
                        "goal:planks",
                        "Blackwood Planks",
                        List.of(stack(planks, "Blackwood Planks", 1)),
                        1,
                        "slot:recipe/planks",
                        "slot:test",
                        List.of(recipe(
                                "slot:recipe/planks",
                                stack(planks, "Blackwood Planks", 1),
                                GoalIngredientDescriptor.concrete("log", stack(log, "Blackwood Log", 1), 1),
                                GoalIngredientDescriptor.choice(
                                        "saw",
                                        "#item:forge:tools/saws",
                                        1,
                                        "#item:forge:tools/saws",
                                        List.of(
                                                stack(wroughtIronSaw, "Wrought Iron Saw", 1),
                                                stack(steelSaw, "Steel Saw", 1)
                                        ))
                        ))
                ),
                GoalVisibleAuthority.fromCounts(Map.of(), Map.of(exactStoredSteelSaw, 1), Map.of())
        );

        GoalProjectionEntry sawEntry = entryFor(projection, steelSaw);
        assertEquals(GoalProjectionEntryKind.STORAGE_GHOST, sawEntry.kind());
        assertEquals(1, sawEntry.storageCount());
        assertEquals(0, sawEntry.missingCount());
        assertTrue(projection.entries().stream()
                .noneMatch(entry -> entry.kind() == GoalProjectionEntryKind.CHOICE_CARD));
    }

    @Test
    void movableExactAuthorityCountsAggregateBeforeProjection() {
        ItemIdentity planks = ItemIdentity.of("tfc:wood/planks/blackwood");
        ItemIdentity steelSaw = ItemIdentity.of("tfc:metal/saw/steel");
        ItemIdentity exactStoredSteelSaw = ItemIdentity.exact("tfc:metal/saw/steel", "damage=12");
        ItemIdentity exactCarriedSteelSaw = ItemIdentity.exact("tfc:metal/saw/steel", "damage=37");

        GoalProjection projection = service.project(
                new GoalDescriptor(
                        "goal:planks",
                        "Blackwood Planks",
                        List.of(stack(planks, "Blackwood Planks", 1)),
                        1,
                        "slot:recipe/planks",
                        "slot:test",
                        List.of(recipe(
                                "slot:recipe/planks",
                                stack(planks, "Blackwood Planks", 1),
                                GoalIngredientDescriptor.concrete("saw", stack(steelSaw, "Steel Saw", 1), 2)
                        ))
                ),
                GoalVisibleAuthority.fromCounts(Map.of(exactCarriedSteelSaw, 1), Map.of(exactStoredSteelSaw, 1), Map.of())
        );

        GoalProjectionEntry sawEntry = entryFor(projection, steelSaw);
        assertEquals(GoalProjectionEntryKind.REAL_CARD, sawEntry.kind());
        assertEquals(1, sawEntry.carriedCount());
        assertEquals(1, sawEntry.storageCount());
        assertEquals(0, sawEntry.missingCount());
        assertEquals(0, sawEntry.wantedCount());
    }

    private static GoalProjectionEntry entryFor(GoalProjection projection, ItemIdentity identity) {
        return projection.entries().stream()
                .filter(entry -> identity.equals(entry.identity()))
                .findFirst()
                .orElseThrow();
    }

    private static GoalDescriptor cokeOvenGoal() {
        return new GoalDescriptor(
                "goal:coke_oven",
                "Coke Oven",
                List.of(stack(COKE_OVEN, "Coke Oven", 1)),
                1,
                "slot:recipe/coke_oven",
                "slot:crafting",
                List.of(
                        recipe(
                                "slot:recipe/coke_oven",
                                stack(COKE_OVEN, "Coke Oven", 1),
                                GoalIngredientDescriptor.concrete(
                                        "brick_blocks",
                                        stack(COKE_BRICK_BLOCK, "Coke Brick Block", 1),
                                        3
                                )
                        ),
                        recipe(
                                "slot:recipe/coke_brick_block",
                                stack(COKE_BRICK_BLOCK, "Coke Brick Block", 1),
                                GoalIngredientDescriptor.concrete(
                                        "bricks",
                                        stack(COKE_BRICK, "Coke Brick", 1),
                                        4
                                )
                        ),
                        recipe(
                                "slot:recipe/coke_brick",
                                stack(COKE_BRICK, "Coke Brick", 1),
                                GoalIngredientDescriptor.concrete("sand", stack(SAND, "Sand", 1), 1),
                                GoalIngredientDescriptor.concrete(
                                        "water",
                                        stack(WATER_BUCKET, "Water Bucket", 1),
                                        1
                                ),
                                GoalIngredientDescriptor.choice(
                                        "binder",
                                        "Any clay binder",
                                        1,
                                        "#forge:clay_binders",
                                        List.of(
                                                stack(CLAY_BALL, "Clay Ball", 1),
                                                stack(BRICK, "Brick", 1)
                                        )
                                )
                        )
                )
        );
    }

    private static GoalDescriptor chainGoal() {
        ItemIdentity a = ItemIdentity.of("slot:a");
        ItemIdentity b = ItemIdentity.of("slot:b");
        ItemIdentity c = ItemIdentity.of("slot:c");
        ItemIdentity d = ItemIdentity.of("slot:d");
        return new GoalDescriptor(
                "goal:a",
                "A",
                List.of(stack(a, "A", 1)),
                1,
                "slot:recipe/a",
                "slot:test",
                List.of(
                        recipe("slot:recipe/a", stack(a, "A", 1),
                                GoalIngredientDescriptor.concrete("b", stack(b, "B", 1), 1)),
                        recipe("slot:recipe/b", stack(b, "B", 1),
                                GoalIngredientDescriptor.concrete("c", stack(c, "C", 1), 1)),
                        recipe("slot:recipe/c", stack(c, "C", 1),
                                GoalIngredientDescriptor.concrete("d", stack(d, "D", 1), 1))
                )
        );
    }

    private static GoalDescriptor loopGoal() {
        ItemIdentity a = ItemIdentity.of("slot:loop_a");
        ItemIdentity b = ItemIdentity.of("slot:loop_b");
        return new GoalDescriptor(
                "goal:loop_a",
                "Loop A",
                List.of(stack(a, "Loop A", 1)),
                1,
                "slot:recipe/loop_a",
                "slot:test",
                List.of(
                        recipe("slot:recipe/loop_a", stack(a, "Loop A", 1),
                                GoalIngredientDescriptor.concrete("b", stack(b, "Loop B", 1), 1)),
                        recipe("slot:recipe/loop_b", stack(b, "Loop B", 1),
                                GoalIngredientDescriptor.concrete("a", stack(a, "Loop A", 1), 1))
                )
        );
    }

    private static GoalRecipeDescriptor recipe(
            String recipeId,
            GoalStackDescriptor output,
            GoalIngredientDescriptor... inputs
    ) {
        return new GoalRecipeDescriptor(
                recipeId,
                "slot:test",
                true,
                List.of(output),
                List.of(inputs),
                List.of(),
                List.of()
        );
    }

    private static GoalStackDescriptor stack(ItemIdentity identity, String displayName, int count) {
        return new GoalStackDescriptor(identity, displayName, count);
    }
}
