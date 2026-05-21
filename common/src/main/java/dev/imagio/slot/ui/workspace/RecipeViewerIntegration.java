package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.core.ItemIdentity;

public final class RecipeViewerIntegration {
    private static final Delegate NOOP = new Delegate() {
    };
    private static volatile Delegate delegate = NOOP;

    private RecipeViewerIntegration() {
    }

    public static void registerDelegate(Delegate nextDelegate) {
        delegate = nextDelegate == null ? NOOP : nextDelegate;
    }

    public static boolean openRecipe(ItemIdentity identity) {
        return delegate.openRecipe(identity);
    }

    public static boolean openUses(ItemIdentity identity) {
        return delegate.openUses(identity);
    }

    public interface Delegate {
        default boolean openRecipe(ItemIdentity identity) {
            return false;
        }

        default boolean openUses(ItemIdentity identity) {
            return false;
        }
    }
}
