package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.SlotCommon;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client -> server "gather target-count items from nearby chests". Carries no
 * fields; desired counts, active kit needs, and proximate chests are
 * resolved server-side from the player's runtime state.
 *
 * <p>Sent by the in-world client-tick handler when the SLOT UI is not
 * mounted. In-screen gather uses the shared workspace action catalog
 * and delegates to the same common service.
 */
public record SlotGatherActiveKitPayload() implements CustomPacketPayload {
    public static final Type<SlotGatherActiveKitPayload> TYPE =
            new Type<>(SlotCommon.id("gather_active_kit"));

    public static final StreamCodec<ByteBuf, SlotGatherActiveKitPayload> STREAM_CODEC =
            StreamCodec.unit(new SlotGatherActiveKitPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
