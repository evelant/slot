package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.SlotCommon;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SlotDepositPutAwayPayload() implements CustomPacketPayload {
    public static final Type<SlotDepositPutAwayPayload> TYPE =
            new Type<>(SlotCommon.id("deposit_put_away"));

    public static final StreamCodec<ByteBuf, SlotDepositPutAwayPayload> STREAM_CODEC =
            StreamCodec.unit(new SlotDepositPutAwayPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
