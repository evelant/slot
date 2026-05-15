package dev.imagio.slot.forge.network;

import dev.imagio.slot.ui.action.WorkspaceActionEnvelope;
import net.minecraft.network.FriendlyByteBuf;

public record ForgeWorkspaceCloseMessage(WorkspaceActionEnvelope envelope) {
    public ForgeWorkspaceCloseMessage {
        if (envelope == null) {
            throw new IllegalArgumentException("envelope must not be null");
        }
    }

    static void encode(ForgeWorkspaceCloseMessage message, FriendlyByteBuf buffer) {
        WorkspaceActionEnvelope envelope = message.envelope();
        buffer.writeUtf(envelope.sessionId());
        buffer.writeInt(envelope.menuContainerId());
        buffer.writeLong(envelope.viewRevision());
    }

    static ForgeWorkspaceCloseMessage decode(FriendlyByteBuf buffer) {
        return new ForgeWorkspaceCloseMessage(new WorkspaceActionEnvelope(
                buffer.readUtf(),
                buffer.readInt(),
                buffer.readLong()));
    }
}
