package dev.imagio.slot.forge.network;

import net.minecraft.network.FriendlyByteBuf;

public record ForgeGatherActiveKitMessage() {
    static void encode(ForgeGatherActiveKitMessage message, FriendlyByteBuf buffer) {
    }

    static ForgeGatherActiveKitMessage decode(FriendlyByteBuf buffer) {
        return new ForgeGatherActiveKitMessage();
    }
}
