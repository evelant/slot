package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;

/**
 * Shared card gesture-to-action decision table for non-drag transfer and
 * cursor interactions. Backends still own event plumbing and platform sends;
 * this class keeps the workspace verb choice from drifting by loader.
 */
public final class WallCardTransferGesturePolicy {
    public static final int PICKUP_MAX = Integer.MAX_VALUE;

    private WallCardTransferGesturePolicy() {
    }

    public static Decision pointerDown(Context context) {
        if (context == null || context.item() == null) {
            return Decision.none();
        }
        if (context.button() != 1) {
            return Decision.none();
        }
        if (context.controlDown()) {
            SlotWorkspaceViewModel.IdentityRef cursor = context.cursorIdentity();
            if (cursor == null || cursor.equals(context.item().identity())) {
                int halfStack = Math.max(1, context.item().displayStack().getMaxStackSize() / 2);
                return Decision.action(Action.PICKUP_TO_CURSOR, halfStack);
            }
            return Decision.none();
        }
        if (context.cursorCarrying()) {
            return Decision.action(Action.CURSOR_CANCEL);
        }
        return Decision.none();
    }

    public static Decision click(Context context) {
        if (context == null || context.item() == null || context.button() != 0) {
            return Decision.none();
        }
        SlotWorkspaceViewModel.AtlasItem item = context.item();
        if (context.cursorCarrying()) {
            SlotWorkspaceViewModel.IdentityRef cursor = context.cursorIdentity();
            if (cursor == null) {
                return Decision.action(Action.CURSOR_SMART_DEPOSIT);
            }
            if (cursor.equals(item.identity())) {
                return Decision.action(Action.PICKUP_TO_CURSOR, PICKUP_MAX);
            }
            return Decision.action(Action.CURSOR_CANCEL_THEN_PICKUP_TO_CURSOR, PICKUP_MAX);
        }
        if (context.shiftDown()) {
            if (context.continuingShiftTake()) {
                if (missingProximatePresence(item)) {
                    return Decision.status("nearby chest data missing");
                }
                if (proximateChestCount(item) > 0) {
                    return Decision.action(Action.TAKE_STACK_BY_IDENTITY);
                }
                return Decision.status("no nearby chest has " + item.name());
            }
            if (missingProximatePresence(item)) {
                return Decision.status("nearby chest data missing");
            }
            if (item.carried()) {
                if (!hasDepositTarget(context)) {
                    return Decision.status("no nearby chest to push " + item.name());
                }
                return Decision.action(Action.DEPOSIT_HOME_TO_LINKED_CHEST);
            }
            if (proximateChestCount(item) > 0) {
                if (context.carriedFreeSlotCount() == 0) {
                    return Decision.status("carry full - drop something first");
                }
                return Decision.action(Action.TAKE_DESIRED_GAP_OR_STACK_BY_IDENTITY);
            }
            return Decision.status("no nearby chest has " + item.name());
        }
        return Decision.action(Action.PICKUP_TO_CURSOR, PICKUP_MAX);
    }

    public static Decision wheel(Context context, int wheelSteps) {
        if (context == null || context.item() == null || wheelSteps == 0 || context.cursorCarrying()) {
            return Decision.none();
        }
        SlotWorkspaceViewModel.AtlasItem item = context.item();
        int magnitude = Math.abs(wheelSteps);
        if (context.wantedAdjustDown()) {
            return Decision.action(Action.ADJUST_WANTED_COUNT, wheelSteps);
        }
        if (context.controlDown()) {
            return Decision.action(Action.ADJUST_PLAYER_DESIRED_COUNT, wheelSteps);
        }
        if (!context.shiftDown()) {
            return Decision.none();
        }
        if (wheelSteps > 0 && context.sidebarActive() && item.carried()) {
            return Decision.action(Action.CROSS_SURFACE_QUICK_MOVE, magnitude);
        }
        if (wheelSteps > 0) {
            if (missingProximatePresence(item)) {
                return Decision.status("nearby chest data missing");
            }
            if (proximateChestCount(item) <= 0) {
                return Decision.status("no nearby chest has " + item.name());
            }
            if (!item.carried() && context.carriedFreeSlotCount() == 0) {
                return Decision.status("carry full - drop something first");
            }
            return Decision.action(Action.TAKE_ONE_BY_IDENTITY, magnitude);
        }
        if (!hasDepositTarget(context)) {
            return Decision.status("no nearby chest to push " + item.name());
        }
        if (!item.carried()) {
            return Decision.status(item.name() + " not carried");
        }
        return Decision.action(Action.DEPOSIT_ONE_HOME_TO_LINKED_CHEST, magnitude);
    }

    public static int proximateChestCount(SlotWorkspaceViewModel.AtlasItem item) {
        if (item == null) {
            return 0;
        }
        int total = 0;
        for (SlotWorkspaceViewModel.ChestPresenceEntry entry : item.presence()) {
            total += entry == null ? 0 : entry.count();
        }
        return total;
    }

    private static boolean missingProximatePresence(SlotWorkspaceViewModel.AtlasItem item) {
        return item != null && item.proximateCount() > 0 && item.presence().isEmpty();
    }

    private static boolean hasDepositTarget(Context context) {
        return context != null && (context.anyChestProximate() || context.activeChestOpen());
    }

    public enum Action {
        NONE,
        STATUS,
        PICKUP_TO_CURSOR,
        CURSOR_CANCEL,
        CURSOR_SMART_DEPOSIT,
        CURSOR_CANCEL_THEN_PICKUP_TO_CURSOR,
        TAKE_DESIRED_GAP_OR_STACK_BY_IDENTITY,
        TAKE_STACK_BY_IDENTITY,
        TAKE_ONE_BY_IDENTITY,
        DEPOSIT_HOME_TO_LINKED_CHEST,
        DEPOSIT_ONE_HOME_TO_LINKED_CHEST,
        CROSS_SURFACE_QUICK_MOVE,
        ADJUST_PLAYER_DESIRED_COUNT,
        ADJUST_WANTED_COUNT
    }

    public record Decision(Action action, int count, String status) {
        public Decision {
            action = action == null ? Action.NONE : action;
            status = status == null ? "" : status;
        }

        public static Decision none() {
            return new Decision(Action.NONE, 0, "");
        }

        public static Decision action(Action action) {
            return new Decision(action, 0, "");
        }

        public static Decision action(Action action, int count) {
            return new Decision(action, count, "");
        }

        public static Decision status(String status) {
            return new Decision(Action.STATUS, 0, status);
        }

        public boolean handled() {
            return action != Action.NONE;
        }
    }

    public record Context(
            SlotWorkspaceViewModel.AtlasItem item,
            int button,
            boolean shiftDown,
            boolean controlDown,
            SlotWorkspaceViewModel.IdentityRef cursorIdentity,
            boolean cursorCarrying,
            boolean sidebarActive,
            int carriedFreeSlotCount,
            boolean anyChestProximate,
            boolean activeChestOpen,
            boolean wantedAdjustDown,
            boolean continuingShiftTake
    ) {
        public Context {
            carriedFreeSlotCount = Math.max(0, carriedFreeSlotCount);
        }

        public Context(
                SlotWorkspaceViewModel.AtlasItem item,
                int button,
                boolean shiftDown,
                boolean controlDown,
                SlotWorkspaceViewModel.IdentityRef cursorIdentity,
                boolean cursorCarrying,
                boolean sidebarActive,
                int carriedFreeSlotCount,
                boolean anyChestProximate
        ) {
            this(
                    item,
                    button,
                    shiftDown,
                    controlDown,
                    cursorIdentity,
                    cursorCarrying,
                    sidebarActive,
                    carriedFreeSlotCount,
                    anyChestProximate,
                    false,
                    false,
                    false);
        }

        public Context(
                SlotWorkspaceViewModel.AtlasItem item,
                int button,
                boolean shiftDown,
                boolean controlDown,
                SlotWorkspaceViewModel.IdentityRef cursorIdentity,
                boolean cursorCarrying,
                boolean sidebarActive,
                int carriedFreeSlotCount,
                boolean anyChestProximate,
                boolean continuingShiftTake
        ) {
            this(
                    item,
                    button,
                    shiftDown,
                    controlDown,
                    cursorIdentity,
                    cursorCarrying,
                    sidebarActive,
                    carriedFreeSlotCount,
                    anyChestProximate,
                    false,
                    false,
                    continuingShiftTake);
        }

        public Context(
                SlotWorkspaceViewModel.AtlasItem item,
                int button,
                boolean shiftDown,
                boolean controlDown,
                SlotWorkspaceViewModel.IdentityRef cursorIdentity,
                boolean cursorCarrying,
                boolean sidebarActive,
                int carriedFreeSlotCount,
                boolean anyChestProximate,
                boolean wantedAdjustDown,
                boolean continuingShiftTake
        ) {
            this(
                    item,
                    button,
                    shiftDown,
                    controlDown,
                    cursorIdentity,
                    cursorCarrying,
                    sidebarActive,
                    carriedFreeSlotCount,
                    anyChestProximate,
                    false,
                    wantedAdjustDown,
                    continuingShiftTake);
        }
    }
}
