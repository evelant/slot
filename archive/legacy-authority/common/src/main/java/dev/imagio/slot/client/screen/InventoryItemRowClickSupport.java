package dev.imagio.slot.client.screen;

import java.util.function.BooleanSupplier;

public final class InventoryItemRowClickSupport {
    private InventoryItemRowClickSupport() {
    }

    public static ClickIntent resolve(
            int button,
            InventoryItemRowSupport.ClickTarget target,
            boolean shiftDown,
            boolean controlDown,
            boolean cursorCarryingStack,
            boolean cursorMatchesRow,
            int rowCount,
            EmptyPrimaryClick emptyPrimaryClick
    ) {
        if (button != 0 && button != 1) {
            return ClickIntent.IGNORED;
        }
        if (button == 0 && target == InventoryItemRowSupport.ClickTarget.ICON) {
            return ClickIntent.OPEN_ICON_MENU;
        }
        if (button == 0 && target == InventoryItemRowSupport.ClickTarget.DESIRED_COUNT) {
            return ClickIntent.EDIT_DESIRED_COUNT;
        }
        if (target == InventoryItemRowSupport.ClickTarget.ACTION) {
            return ClickIntent.OPEN_ACTION_MENU;
        }

        if (cursorCarryingStack) {
            return button == 1 && cursorMatchesRow
                    ? ClickIntent.PICKUP_HALF
                    : ClickIntent.DROP_CURSOR;
        }

        if (button == 1) {
            return rowCount > 0 ? ClickIntent.PICKUP_HALF : ClickIntent.IGNORED;
        }
        if (shiftDown) {
            return ClickIntent.MOVE_ALL;
        }
        if (controlDown) {
            return ClickIntent.MOVE_ONE;
        }
        if (rowCount > 0) {
            return ClickIntent.PICKUP_STACK;
        }
        return emptyPrimaryClick == EmptyPrimaryClick.START_HOTBAR_DRAG
                ? ClickIntent.START_HOTBAR_DRAG
                : ClickIntent.CONSUME;
    }

    public static boolean execute(ClickIntent intent, RowClickActions actions) {
        if (intent == null || actions == null) {
            return false;
        }
        return switch (intent) {
            case IGNORED -> false;
            case OPEN_ICON_MENU -> {
                run(actions.openIconMenu());
                yield true;
            }
            case EDIT_DESIRED_COUNT -> {
                run(actions.editDesiredCount());
                yield true;
            }
            case OPEN_ACTION_MENU -> {
                run(actions.openActionMenu());
                yield true;
            }
            case PICKUP_HALF -> get(actions.pickupHalf());
            case DROP_CURSOR -> get(actions.dropCursor());
            case MOVE_ALL -> {
                run(actions.moveAll());
                yield true;
            }
            case MOVE_ONE -> {
                run(actions.moveOne());
                yield true;
            }
            case PICKUP_STACK -> {
                if (get(actions.pickupStack())) {
                    yield true;
                }
                run(actions.startHotbarDrag());
                yield true;
            }
            case START_HOTBAR_DRAG -> {
                run(actions.startHotbarDrag());
                yield true;
            }
            case CONSUME -> true;
        };
    }

    private static void run(Runnable action) {
        if (action != null) {
            action.run();
        }
    }

    private static boolean get(BooleanSupplier action) {
        return action != null && action.getAsBoolean();
    }

    public enum EmptyPrimaryClick {
        START_HOTBAR_DRAG,
        CONSUME
    }

    public enum ClickIntent {
        IGNORED,
        OPEN_ICON_MENU,
        EDIT_DESIRED_COUNT,
        OPEN_ACTION_MENU,
        PICKUP_HALF,
        DROP_CURSOR,
        MOVE_ALL,
        MOVE_ONE,
        PICKUP_STACK,
        START_HOTBAR_DRAG,
        CONSUME
    }

    public record RowClickActions(
            Runnable openIconMenu,
            Runnable editDesiredCount,
            Runnable openActionMenu,
            BooleanSupplier pickupHalf,
            BooleanSupplier dropCursor,
            Runnable moveAll,
            Runnable moveOne,
            BooleanSupplier pickupStack,
            Runnable startHotbarDrag
    ) {
    }
}
