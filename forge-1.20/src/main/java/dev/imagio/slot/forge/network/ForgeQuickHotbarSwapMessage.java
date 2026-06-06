package dev.imagio.slot.forge.network;

import net.minecraft.network.FriendlyByteBuf;

public record ForgeQuickHotbarSwapMessage(int direction) {
    static void encode(ForgeQuickHotbarSwapMessage message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.direction());
    }

    static ForgeQuickHotbarSwapMessage decode(FriendlyByteBuf buffer) {
        return new ForgeQuickHotbarSwapMessage(buffer.readInt());
    }
}
