package dev.imagio.slot.forge.network;

import net.minecraft.network.FriendlyByteBuf;

public record ForgeTrashIdentityMessage(
        String itemId,
        String comparisonMode,
        String componentFingerprint
) {
    public ForgeTrashIdentityMessage {
        itemId = itemId == null ? "" : itemId.trim();
        comparisonMode = comparisonMode == null ? "" : comparisonMode.trim();
        componentFingerprint = componentFingerprint == null ? "" : componentFingerprint.trim();
    }

    public static void encode(ForgeTrashIdentityMessage message, FriendlyByteBuf buffer) {
        ForgeTrashIdentityMessage resolved = message == null
                ? new ForgeTrashIdentityMessage("", "", "")
                : message;
        buffer.writeUtf(resolved.itemId());
        buffer.writeUtf(resolved.comparisonMode());
        buffer.writeUtf(resolved.componentFingerprint());
    }

    public static ForgeTrashIdentityMessage decode(FriendlyByteBuf buffer) {
        return new ForgeTrashIdentityMessage(
                buffer.readUtf(),
                buffer.readUtf(),
                buffer.readUtf());
    }
}
