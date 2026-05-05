package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.SlotCommon;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SlotSidebarOpenPayload() implements CustomPacketPayload {
    public static final Type<SlotSidebarOpenPayload> TYPE = new Type<>(SlotCommon.id("sidebar_open"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SlotSidebarOpenPayload> STREAM_CODEC =
            StreamCodec.unit(new SlotSidebarOpenPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
