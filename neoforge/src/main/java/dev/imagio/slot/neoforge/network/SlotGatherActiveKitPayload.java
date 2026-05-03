package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.SlotCommon;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client → server "gather active kit from nearby chests". Carries no
 * fields — the active kit and proximate chests are resolved server-
 * side from the player's runtime state.
 *
 * <p>Sent by both the in-world client-tick handler (when the SLOT UI
 * is not mounted) and the in-screen action overlay's Gather button so
 * one server path covers both contexts.
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
