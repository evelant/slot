package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.SlotCommon;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SlotTrashIdentityPayload(
        String itemId,
        String comparisonMode,
        String componentFingerprint
) implements CustomPacketPayload {
    public static final Type<SlotTrashIdentityPayload> TYPE =
            new Type<>(SlotCommon.id("trash_identity"));

    public static final StreamCodec<ByteBuf, SlotTrashIdentityPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    SlotTrashIdentityPayload::itemId,
                    ByteBufCodecs.STRING_UTF8,
                    SlotTrashIdentityPayload::comparisonMode,
                    ByteBufCodecs.STRING_UTF8,
                    SlotTrashIdentityPayload::componentFingerprint,
                    SlotTrashIdentityPayload::new);

    public SlotTrashIdentityPayload {
        itemId = itemId == null ? "" : itemId.trim();
        comparisonMode = comparisonMode == null ? "" : comparisonMode.trim();
        componentFingerprint = componentFingerprint == null ? "" : componentFingerprint.trim();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
