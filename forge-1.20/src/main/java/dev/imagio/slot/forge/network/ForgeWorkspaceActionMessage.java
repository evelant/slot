package dev.imagio.slot.forge.network;

import dev.imagio.slot.ui.action.WorkspaceActionPacket;
import dev.imagio.slot.ui.action.WorkspaceActionPacketCodec;
import net.minecraft.network.FriendlyByteBuf;

public record ForgeWorkspaceActionMessage(WorkspaceActionPacket packet) {
    public ForgeWorkspaceActionMessage {
        if (packet == null) {
            throw new IllegalArgumentException("packet must not be null");
        }
    }

    static void encode(ForgeWorkspaceActionMessage message, FriendlyByteBuf buffer) {
        WorkspaceActionPacketCodec.write(
                new Forge120WorkspaceActionPacketBuffer(buffer),
                message.packet());
    }

    static ForgeWorkspaceActionMessage decode(FriendlyByteBuf buffer) {
        return new ForgeWorkspaceActionMessage(WorkspaceActionPacketCodec.read(
                new Forge120WorkspaceActionPacketBuffer(buffer)));
    }
}
