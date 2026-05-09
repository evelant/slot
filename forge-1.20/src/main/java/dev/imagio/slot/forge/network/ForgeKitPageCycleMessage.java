package dev.imagio.slot.forge.network;

import net.minecraft.network.FriendlyByteBuf;

public record ForgeKitPageCycleMessage(int direction) {
    static void encode(ForgeKitPageCycleMessage message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.direction());
    }

    static ForgeKitPageCycleMessage decode(FriendlyByteBuf buffer) {
        return new ForgeKitPageCycleMessage(buffer.readInt());
    }
}
