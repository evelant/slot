package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.SlotCommon;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client → server kit-page cycle. Carries a signed direction (+1
 * forward, -1 backward) so the same handler covers both forward and
 * shift-modified backward presses without overloading the payload type.
 *
 * <p>Sent by the in-world client-tick handler when the SLOT UI is not
 * mounted; the in-screen path goes through the workspace RPC dispatcher
 * instead.
 */
public record SlotKitPageCyclePayload(int direction) implements CustomPacketPayload {
    public static final Type<SlotKitPageCyclePayload> TYPE =
            new Type<>(SlotCommon.id("kit_page_cycle"));

    public static final StreamCodec<ByteBuf, SlotKitPageCyclePayload> STREAM_CODEC =
            ByteBufCodecs.INT.map(SlotKitPageCyclePayload::new, SlotKitPageCyclePayload::direction);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
