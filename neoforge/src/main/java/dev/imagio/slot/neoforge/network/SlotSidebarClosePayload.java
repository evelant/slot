package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.SlotCommon;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SlotSidebarClosePayload() implements CustomPacketPayload {
    public static final Type<SlotSidebarClosePayload> TYPE = new Type<>(SlotCommon.id("sidebar_close"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SlotSidebarClosePayload> STREAM_CODEC =
            StreamCodec.unit(new SlotSidebarClosePayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
