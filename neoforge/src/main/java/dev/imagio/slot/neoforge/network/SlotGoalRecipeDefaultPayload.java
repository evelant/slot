package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.SlotCommon;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SlotGoalRecipeDefaultPayload(String outputItemId, String recipeId) implements CustomPacketPayload {
    public static final Type<SlotGoalRecipeDefaultPayload> TYPE =
            new Type<>(SlotCommon.id("goal_recipe_default"));

    public static final StreamCodec<ByteBuf, SlotGoalRecipeDefaultPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    SlotGoalRecipeDefaultPayload::outputItemId,
                    ByteBufCodecs.STRING_UTF8,
                    SlotGoalRecipeDefaultPayload::recipeId,
                    SlotGoalRecipeDefaultPayload::new);

    public SlotGoalRecipeDefaultPayload {
        outputItemId = outputItemId == null ? "" : outputItemId.trim();
        recipeId = recipeId == null ? "" : recipeId.trim();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
