package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.ui.action.WorkspaceActionId;

import java.util.Objects;

/**
 * Coalesces rapid shift-wheel storage transfers before the platform adapter
 * sends them. The server still owns the authoritative mutation; this only
 * preserves "N wheel steps" as one counted intent instead of N packets.
 */
public final class WheelTransferBatcher {
    static final int DEFAULT_IDLE_FLUSH_TICKS = 2;

    private final int idleFlushTicks;
    private WorkspaceActionId action;
    private SlotWorkspaceViewModel.IdentityRef identity;
    private int count;
    private String status;
    private int ticksUntilFlush;

    public WheelTransferBatcher() {
        this(DEFAULT_IDLE_FLUSH_TICKS);
    }

    WheelTransferBatcher(int idleFlushTicks) {
        this.idleFlushTicks = Math.max(1, idleFlushTicks);
    }

    public Pending enqueue(
            WorkspaceActionId nextAction,
            SlotWorkspaceViewModel.IdentityRef nextIdentity,
            int nextCount,
            String nextStatus
    ) {
        int resolvedCount = Math.max(0, nextCount);
        if (nextAction == null || nextIdentity == null || resolvedCount == 0) {
            return null;
        }
        if (action == null || sameBatch(nextAction, nextIdentity)) {
            accept(nextAction, nextIdentity, resolvedCount, nextStatus);
            return null;
        }
        Pending flushed = flush();
        accept(nextAction, nextIdentity, resolvedCount, nextStatus);
        return flushed;
    }

    public Pending flushIfIdle() {
        if (action == null || identity == null || count <= 0) {
            clear();
            return null;
        }
        ticksUntilFlush--;
        return ticksUntilFlush <= 0 ? flush() : null;
    }

    public Pending flush() {
        if (action == null || identity == null || count <= 0) {
            clear();
            return null;
        }
        Pending pending = new Pending(action, identity, count, status);
        clear();
        return pending;
    }

    private void accept(
            WorkspaceActionId nextAction,
            SlotWorkspaceViewModel.IdentityRef nextIdentity,
            int nextCount,
            String nextStatus
    ) {
        action = nextAction;
        identity = nextIdentity;
        count = saturatingAdd(count, nextCount);
        status = nextStatus == null ? "" : nextStatus;
        ticksUntilFlush = idleFlushTicks;
    }

    private boolean sameBatch(WorkspaceActionId nextAction, SlotWorkspaceViewModel.IdentityRef nextIdentity) {
        return action == nextAction && Objects.equals(identity, nextIdentity);
    }

    private static int saturatingAdd(int left, int right) {
        if (Integer.MAX_VALUE - left < right) {
            return Integer.MAX_VALUE;
        }
        return left + right;
    }

    private void clear() {
        action = null;
        identity = null;
        count = 0;
        status = "";
        ticksUntilFlush = 0;
    }

    public record Pending(
            WorkspaceActionId action,
            SlotWorkspaceViewModel.IdentityRef identity,
            int count,
            String status
    ) {
        public Pending {
            count = Math.max(0, count);
            status = status == null ? "" : status;
        }
    }
}
