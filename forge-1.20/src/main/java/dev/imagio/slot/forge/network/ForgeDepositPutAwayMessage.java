package dev.imagio.slot.forge.network;

import net.minecraft.network.FriendlyByteBuf;

public record ForgeDepositPutAwayMessage() {
    static void encode(ForgeDepositPutAwayMessage message, FriendlyByteBuf buffer) {
    }

    static ForgeDepositPutAwayMessage decode(FriendlyByteBuf buffer) {
        return new ForgeDepositPutAwayMessage();
    }
}
