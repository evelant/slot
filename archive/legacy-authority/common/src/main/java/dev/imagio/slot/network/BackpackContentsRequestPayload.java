package dev.imagio.slot.network;

import dev.imagio.slot.SlotCommon;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record BackpackContentsRequestPayload() implements CustomPacketPayload {
    public static final Type<BackpackContentsRequestPayload> TYPE = new Type<>(SlotCommon.id("backpack_contents_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BackpackContentsRequestPayload> STREAM_CODEC = StreamCodec.unit(new BackpackContentsRequestPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
