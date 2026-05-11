package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.goal.GoalDescriptor;

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

    public static boolean openUses(ItemIdentity identity) {
        return delegate.openUses(identity);
    }

    public static boolean openChoiceEditor(GoalDescriptor goal, String choiceGroupId) {
        return delegate.openChoiceEditor(goal, choiceGroupId);
    }

    public static boolean openWorkspace() {
        return delegate.openWorkspace();
    }

    public interface Delegate {
        default boolean openRecipe(GoalDescriptor goal) {
            return false;
        }

        default boolean openUses(ItemIdentity identity) {
            return false;
        }

        default boolean openChoiceEditor(GoalDescriptor goal, String choiceGroupId) {
            return false;
        }

        default boolean openWorkspace() {
            return false;
        }
    }
}
