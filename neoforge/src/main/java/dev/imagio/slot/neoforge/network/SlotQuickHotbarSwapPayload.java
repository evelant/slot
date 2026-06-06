package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.SlotCommon;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SlotQuickHotbarSwapPayload(int direction) implements CustomPacketPayload {
    public static final Type<SlotQuickHotbarSwapPayload> TYPE =
            new Type<>(SlotCommon.id("quick_hotbar_swap"));

    public static final StreamCodec<ByteBuf, SlotQuickHotbarSwapPayload> STREAM_CODEC =
            ByteBufCodecs.INT.map(SlotQuickHotbarSwapPayload::new, SlotQuickHotbarSwapPayload::direction);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
