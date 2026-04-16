package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.SlotCommon;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SlotWorkspaceOpenPayload() implements CustomPacketPayload {
    public static final Type<SlotWorkspaceOpenPayload> TYPE = new Type<>(SlotCommon.id("workspace_open"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SlotWorkspaceOpenPayload> STREAM_CODEC =
            StreamCodec.unit(new SlotWorkspaceOpenPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
