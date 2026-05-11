package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.goal.GoalDescriptor;
import dev.imagio.slot.inventory.goal.GoalIngredientDescriptor;
import dev.imagio.slot.inventory.goal.GoalRecipeDescriptor;
import dev.imagio.slot.inventory.goal.GoalStackDescriptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GoalWorkspaceClientStateTest {
    @AfterEach
    void clearState() {
        GoalWorkspaceClientState.clear();
    }

    @Test
    void addsActivatesAdjustsAndRemovesGoalTabs() {
        GoalWorkspaceClientState.clear();

        GoalWorkspaceClientState.GoalTab added = GoalWorkspaceClientState.addOrActivate(goal("slot:recipe/a", "A", 2));

        assertEquals("emi:slot:recipe/a", added.goalId());
        assertTrue(added.active());
        assertEquals(2, added.targetCount());
        assertEquals(1, GoalWorkspaceClientState.goalTabs().size());

        assertTrue(GoalWorkspaceClientState.adjustTargetCount(added.goalId(), 3));
        assertEquals(5, GoalWorkspaceClientState.activeGoal().targetCount());
        assertEquals(5, GoalWorkspaceClientState.activeGoal().descriptor().targetCount());

        GoalWorkspaceClientState.selectAll();
        assertFalse(GoalWorkspaceClientState.hasActiveGoal());

        assertTrue(GoalWorkspaceClientState.selectGoal(added.goalId()));
        assertTrue(GoalWorkspaceClientState.removeGoal(added.goalId()));
        assertNull(GoalWorkspaceClientState.activeGoal());
        assertTrue(GoalWorkspaceClientState.goalTabs().isEmpty());
    }

    private static GoalDescriptor goal(String recipeId, String label, int count) {
        ItemIdentity output = ItemIdentity.of("slot:" + label.toLowerCase());
        GoalStackDescriptor outputStack = new GoalStackDescriptor(output, label, count);
        return new GoalDescriptor(
                "emi:" + recipeId,
                label,
                List.of(outputStack),
                count,
                recipeId,
                "slot:test",
                List.of(new GoalRecipeDescriptor(
                        recipeId,
                        "slot:test",
                        true,
                        List.of(outputStack),
                        List.of(GoalIngredientDescriptor.concrete(
                                "input",
                                GoalStackDescriptor.of("minecraft:stone", 1),
                                1
                        )),
                        List.of(),
                        List.of()
                ))
        );
    }
}
