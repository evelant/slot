package dev.imagio.slot.forge.network;

import dev.imagio.slot.ui.action.WorkspaceActionEnvelope;
import net.minecraft.network.FriendlyByteBuf;

public record ForgeWorkspaceRefreshMessage(WorkspaceActionEnvelope envelope) {
    public ForgeWorkspaceRefreshMessage {
        if (envelope == null) {
            throw new IllegalArgumentException("envelope must not be null");
        }
    }

    static void encode(ForgeWorkspaceRefreshMessage message, FriendlyByteBuf buffer) {
        WorkspaceActionEnvelope envelope = message.envelope();
        buffer.writeUtf(envelope.sessionId());
        buffer.writeInt(envelope.menuContainerId());
        buffer.writeLong(envelope.viewRevision());
    }

    static ForgeWorkspaceRefreshMessage decode(FriendlyByteBuf buffer) {
        return new ForgeWorkspaceRefreshMessage(new WorkspaceActionEnvelope(
                buffer.readUtf(),
                buffer.readInt(),
                buffer.readLong()));
    }
}
