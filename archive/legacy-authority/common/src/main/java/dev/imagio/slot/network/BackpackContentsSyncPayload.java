package dev.imagio.slot.network;

import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import dev.imagio.slot.SlotCommon;

import java.util.UUID;

public record BackpackContentsSyncPayload(UUID backpackUuid, CompoundTag backpackContents) implements CustomPacketPayload {
    public static final Type<BackpackContentsSyncPayload> TYPE = new Type<>(SlotCommon.id("backpack_contents_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BackpackContentsSyncPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public BackpackContentsSyncPayload decode(RegistryFriendlyByteBuf buf) {
            return new BackpackContentsSyncPayload(
                    UUIDUtil.STREAM_CODEC.decode(buf),
                    buf.readNbt()
            );
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, BackpackContentsSyncPayload payload) {
            UUIDUtil.STREAM_CODEC.encode(buf, payload.backpackUuid);
            buf.writeNbt(payload.backpackContents);
        }
    };

    public BackpackContentsSyncPayload {
        backpackContents = backpackContents == null ? new CompoundTag() : backpackContents.copy();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
