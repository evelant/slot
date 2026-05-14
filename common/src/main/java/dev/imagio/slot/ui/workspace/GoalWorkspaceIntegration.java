package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.goal.GoalDescriptor;
import dev.imagio.slot.inventory.goal.GoalProjectionEntry;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;

public final class GoalWorkspaceIntegration {
    private static final Delegate NOOP = new Delegate() {
    };
    private static volatile Delegate delegate = NOOP;

    private GoalWorkspaceIntegration() {
    }

    public static void registerDelegate(Delegate nextDelegate) {
        delegate = nextDelegate == null ? NOOP : nextDelegate;
    }

    public static boolean openRecipe(GoalDescriptor goal) {
        return delegate.openRecipe(goal);
    }

    public static boolean openRecipe(GoalDescriptor goal, GoalProjectionEntry entry) {
        return delegate.openRecipe(goal, entry);
    }

    public static boolean openRecipe(ItemIdentity identity) {
        return delegate.openRecipe(identity);
    }

    public static boolean openUses(ItemIdentity identity) {
        return delegate.openUses(identity);
    }

    public static boolean openChoiceEditor(GoalDescriptor goal, String choiceGroupId) {
        return delegate.openChoiceEditor(goal, choiceGroupId);
    }

    public static boolean openChoiceEditor(GoalDescriptor goal, GoalProjectionEntry entry) {
        return delegate.openChoiceEditor(goal, entry);
    }

    public static boolean openWorkspace() {
        return delegate.openWorkspace();
    }

    public static boolean persistGoal(GoalWorkspaceClientState.GoalTab goal) {
        return delegate.persistGoal(goal);
    }

    public static boolean removePersistedGoal(String goalId) {
        return delegate.removePersistedGoal(goalId);
    }

    public static GoalDescriptor enrichVisibleAlternatives(GoalDescriptor goal, SlotWorkspaceViewModel source) {
        GoalDescriptor enriched = delegate.enrichVisibleAlternatives(goal, source);
        return enriched == null ? goal : enriched;
    }

    public interface Delegate {
        default boolean openRecipe(GoalDescriptor goal) {
            return false;
        }

        default boolean openRecipe(GoalDescriptor goal, GoalProjectionEntry entry) {
            return openRecipe(goal);
        }

        default boolean openRecipe(ItemIdentity identity) {
            return false;
        }

        default boolean openUses(ItemIdentity identity) {
            return false;
        }

        default boolean openChoiceEditor(GoalDescriptor goal, String choiceGroupId) {
            return false;
        }

        default boolean openChoiceEditor(GoalDescriptor goal, GoalProjectionEntry entry) {
            return openChoiceEditor(goal, entry == null ? "" : entry.choiceGroupId());
        }

        default boolean openWorkspace() {
            return false;
        }

        default boolean persistGoal(GoalWorkspaceClientState.GoalTab goal) {
            return false;
        }

        default boolean removePersistedGoal(String goalId) {
            return false;
        }

        default GoalDescriptor enrichVisibleAlternatives(GoalDescriptor goal, SlotWorkspaceViewModel source) {
            return goal;
        }
    }
}
