package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.SlotCommon;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public record SlotChestUnclaimPayload(
        ResourceKey<Level> dimension,
        BlockPos pos
) implements CustomPacketPayload {
    public static final Type<SlotChestUnclaimPayload> TYPE = new Type<>(SlotCommon.id("chest_unclaim"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SlotChestUnclaimPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC.map(
                            id -> ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, id),
                            ResourceKey::location
                    ),
                    SlotChestUnclaimPayload::dimension,
                    BlockPos.STREAM_CODEC,
                    SlotChestUnclaimPayload::pos,
                    SlotChestUnclaimPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
