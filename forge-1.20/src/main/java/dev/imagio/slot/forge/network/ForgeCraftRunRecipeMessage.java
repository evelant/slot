package dev.imagio.slot.forge.network;

import dev.imagio.slot.workflow.domain.CraftRunRecipeCapture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public record ForgeCraftRunRecipeMessage(CraftRunRecipeCapture capture) {
    public ForgeCraftRunRecipeMessage {
        capture = capture == null ? CraftRunRecipeCapture.empty() : capture;
    }

    public static ForgeCraftRunRecipeMessage add(CraftRunRecipeCapture capture) {
        return new ForgeCraftRunRecipeMessage(capture);
    }

    public static void encode(ForgeCraftRunRecipeMessage message, FriendlyByteBuf buffer) {
        ForgeCraftRunRecipeMessage resolved = message == null ? add(CraftRunRecipeCapture.empty()) : message;
        buffer.writeNbt(Forge120WorkspaceViewModelCodec.encodeCraftRunRecipeCapture(resolved.capture()));
    }

    public static ForgeCraftRunRecipeMessage decode(FriendlyByteBuf buffer) {
        CompoundTag tag = buffer.readNbt();
        return new ForgeCraftRunRecipeMessage(Forge120WorkspaceViewModelCodec.decodeCraftRunRecipeCapture(tag));
    }
}
