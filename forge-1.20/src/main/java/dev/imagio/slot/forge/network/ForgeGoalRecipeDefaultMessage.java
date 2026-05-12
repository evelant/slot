package dev.imagio.slot.forge.network;

import net.minecraft.network.FriendlyByteBuf;

public record ForgeGoalRecipeDefaultMessage(String outputItemId, String recipeId) {
    public ForgeGoalRecipeDefaultMessage {
        outputItemId = outputItemId == null ? "" : outputItemId.trim();
        recipeId = recipeId == null ? "" : recipeId.trim();
    }

    public static void encode(ForgeGoalRecipeDefaultMessage message, FriendlyByteBuf buffer) {
        ForgeGoalRecipeDefaultMessage resolved = message == null
                ? new ForgeGoalRecipeDefaultMessage("", "")
                : message;
        buffer.writeUtf(resolved.outputItemId());
        buffer.writeUtf(resolved.recipeId());
    }

    public static ForgeGoalRecipeDefaultMessage decode(FriendlyByteBuf buffer) {
        return new ForgeGoalRecipeDefaultMessage(buffer.readUtf(), buffer.readUtf());
    }
}
