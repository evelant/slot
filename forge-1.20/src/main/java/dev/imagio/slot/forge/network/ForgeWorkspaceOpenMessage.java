package dev.imagio.slot.forge.network;

import dev.imagio.slot.ui.action.WorkspaceActionEnvelope;
import net.minecraft.network.FriendlyByteBuf;

public record ForgeWorkspaceOpenMessage(WorkspaceActionEnvelope envelope) {
    public ForgeWorkspaceOpenMessage {
        if (envelope == null) {
            throw new IllegalArgumentException("envelope must not be null");
        }
    }

    static void encode(ForgeWorkspaceOpenMessage message, FriendlyByteBuf buffer) {
        WorkspaceActionEnvelope envelope = message.envelope();
        buffer.writeUtf(envelope.sessionId());
        buffer.writeInt(envelope.menuContainerId());
        buffer.writeLong(envelope.viewRevision());
    }

    static ForgeWorkspaceOpenMessage decode(FriendlyByteBuf buffer) {
        return new ForgeWorkspaceOpenMessage(new WorkspaceActionEnvelope(
                buffer.readUtf(),
                buffer.readInt(),
                buffer.readLong()));
    }
}
