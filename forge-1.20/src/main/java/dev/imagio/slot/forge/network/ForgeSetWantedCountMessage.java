package dev.imagio.slot.forge.network;

import net.minecraft.network.FriendlyByteBuf;

public record ForgeSetWantedCountMessage(
        String itemId,
        String comparisonMode,
        String componentFingerprint,
        int targetCount
) {
    public ForgeSetWantedCountMessage {
        itemId = itemId == null ? "" : itemId.trim();
        comparisonMode = comparisonMode == null ? "" : comparisonMode.trim();
        componentFingerprint = componentFingerprint == null ? "" : componentFingerprint.trim();
        targetCount = Math.max(0, targetCount);
    }

    public static void encode(ForgeSetWantedCountMessage message, FriendlyByteBuf buffer) {
        ForgeSetWantedCountMessage resolved = message == null
                ? new ForgeSetWantedCountMessage("", "", "", 0)
                : message;
        buffer.writeUtf(resolved.itemId());
        buffer.writeUtf(resolved.comparisonMode());
        buffer.writeUtf(resolved.componentFingerprint());
        buffer.writeVarInt(resolved.targetCount());
    }

    public static ForgeSetWantedCountMessage decode(FriendlyByteBuf buffer) {
        return new ForgeSetWantedCountMessage(
                buffer.readUtf(),
                buffer.readUtf(),
                buffer.readUtf(),
                buffer.readVarInt());
    }
}
