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
    void projectsFixtureGoalWithDesiredCountsGhostsAndChoiceCards() {
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
        assertEquals(3, projection.desiredCounts().get(SAND));
        assertEquals(5, projection.desiredCounts().get(WATER_BUCKET));
        assertFalse(projection.desiredCounts().containsKey(COKE_BRICK_BLOCK));
        assertFalse(projection.desiredCounts().containsKey(COKE_BRICK));

        GoalProjectionEntry block = entryFor(projection, COKE_BRICK_BLOCK);
        assertEquals(GoalProjectionEntryKind.STORAGE_GHOST, block.kind());
        assertEquals(3, block.requiredCount());
        assertEquals(1, block.storageCount());
        assertEquals(2, block.missingCount());
        assertEquals(0, block.desiredCount());

        GoalProjectionEntry sand = entryFor(projection, SAND);
        assertEquals(GoalProjectionEntryKind.REAL_CARD, sand.kind());
        assertEquals(5, sand.requiredCount());
        assertEquals(2, sand.carriedCount());
        assertEquals(3, sand.desiredCount());

        GoalProjectionEntry water = entryFor(projection, WATER_BUCKET);
        assertEquals(GoalProjectionEntryKind.MISSING_GHOST, water.kind());
        assertEquals(5, water.requiredCount());
        assertEquals(5, water.desiredCount());
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
