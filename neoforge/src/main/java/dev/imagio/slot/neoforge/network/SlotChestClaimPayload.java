package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.SlotCommon;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.UUID;

public record SlotChestClaimPayload(
        ResourceKey<Level> dimension,
        BlockPos pos,
        String areaId,
        String newAreaLabel
) implements CustomPacketPayload {
    public static final Type<SlotChestClaimPayload> TYPE = new Type<>(SlotCommon.id("chest_claim"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SlotChestClaimPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC.map(
                            id -> ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, id),
                            ResourceKey::location
                    ),
                    SlotChestClaimPayload::dimension,
                    BlockPos.STREAM_CODEC,
                    SlotChestClaimPayload::pos,
                    ByteBufCodecs.STRING_UTF8,
                    SlotChestClaimPayload::areaId,
                    ByteBufCodecs.STRING_UTF8,
                    SlotChestClaimPayload::newAreaLabel,
                    SlotChestClaimPayload::new
            );

    public SlotChestClaimPayload(ResourceKey<Level> dimension, BlockPos pos) {
        this(dimension, pos, "", "");
    }

    public SlotChestClaimPayload(ResourceKey<Level> dimension, BlockPos pos, String areaId) {
        this(dimension, pos, areaId == null ? "" : areaId, "");
    }

    public UUID requestedAreaId() {
        if (areaId == null || areaId.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(areaId);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    public String newAreaLabelTrimmed() {
        return newAreaLabel == null ? "" : newAreaLabel.trim();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
