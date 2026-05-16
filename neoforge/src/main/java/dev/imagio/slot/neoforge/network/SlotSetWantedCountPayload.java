package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.SlotCommon;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SlotSetWantedCountPayload(
        String itemId,
        String comparisonMode,
        String componentFingerprint,
        int targetCount
) implements CustomPacketPayload {
    public static final Type<SlotSetWantedCountPayload> TYPE =
            new Type<>(SlotCommon.id("set_wanted_count"));

    public static final StreamCodec<ByteBuf, SlotSetWantedCountPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    SlotSetWantedCountPayload::itemId,
                    ByteBufCodecs.STRING_UTF8,
                    SlotSetWantedCountPayload::comparisonMode,
                    ByteBufCodecs.STRING_UTF8,
                    SlotSetWantedCountPayload::componentFingerprint,
                    ByteBufCodecs.VAR_INT,
                    SlotSetWantedCountPayload::targetCount,
                    SlotSetWantedCountPayload::new);

    public SlotSetWantedCountPayload {
        itemId = itemId == null ? "" : itemId.trim();
        comparisonMode = comparisonMode == null ? "" : comparisonMode.trim();
        componentFingerprint = componentFingerprint == null ? "" : componentFingerprint.trim();
        targetCount = Math.max(0, targetCount);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
