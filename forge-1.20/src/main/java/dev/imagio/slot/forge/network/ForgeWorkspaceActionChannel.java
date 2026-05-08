package dev.imagio.slot.forge.network;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.ui.action.WorkspaceActionChannel;
import dev.imagio.slot.ui.action.WorkspaceActionEnvelope;
import dev.imagio.slot.ui.action.WorkspaceActionId;
import dev.imagio.slot.ui.action.WorkspaceActionPacket;

public final class ForgeWorkspaceActionChannel implements WorkspaceActionChannel {
    private final WorkspaceActionEnvelope envelope;

    public ForgeWorkspaceActionChannel(WorkspaceActionEnvelope envelope) {
        this.envelope = envelope;
    }

    @Override
    public boolean send(WorkspaceActionId action, Object... arguments) {
        WorkspaceActionPacket packet;
        try {
            packet = WorkspaceActionPacket.fromObjects(envelope, action, arguments);
        } catch (IllegalArgumentException exception) {
            SlotCommon.LOGGER.warn("Rejected Forge workspace action before send: action={} diagnostics={}",
                    action,
                    exception.getMessage());
            return false;
        }
        return SlotForgeNetworking.sendToServer(new ForgeWorkspaceActionMessage(packet));
    }
}
