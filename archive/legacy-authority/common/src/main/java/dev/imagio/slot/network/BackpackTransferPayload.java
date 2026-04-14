package dev.imagio.slot.network;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.client.model.ComparisonMode;
import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.client.screen.container.MenuSlotId;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record BackpackTransferPayload(
        int containerId,
        Direction direction,
        Mode mode,
        int requestedCount,
        int menuSlot,
        String itemId,
        ComparisonMode comparisonMode,
        String componentFingerprint
) implements CustomPacketPayload {
    public static final Type<BackpackTransferPayload> TYPE = new Type<>(SlotCommon.id("backpack_transfer"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BackpackTransferPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public BackpackTransferPayload decode(RegistryFriendlyByteBuf buf) {
            return new BackpackTransferPayload(
                    buf.readVarInt(),
                    buf.readEnum(Direction.class),
                    buf.readEnum(Mode.class),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readUtf(),
                    buf.readEnum(ComparisonMode.class),
                    buf.readUtf()
            );
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, BackpackTransferPayload payload) {
            buf.writeVarInt(payload.containerId);
            buf.writeEnum(payload.direction);
            buf.writeEnum(payload.mode);
            buf.writeVarInt(payload.requestedCount);
            buf.writeVarInt(payload.menuSlot);
            buf.writeUtf(payload.itemId);
            buf.writeEnum(payload.comparisonMode);
            buf.writeUtf(payload.componentFingerprint);
        }
    };

    public BackpackTransferPayload {
        direction = direction == null ? Direction.EXTERNAL_TO_CARRIED : direction;
        mode = mode == null ? Mode.STACK : mode;
        requestedCount = Math.max(0, requestedCount);
        itemId = itemId == null ? "" : itemId;
        comparisonMode = comparisonMode == null ? ComparisonMode.ITEM_ID : comparisonMode;
        componentFingerprint = componentFingerprint == null ? "" : componentFingerprint;
    }

    public static BackpackTransferPayload externalToCarried(int containerId, int menuSlot, Mode mode) {
        return new BackpackTransferPayload(containerId, Direction.EXTERNAL_TO_CARRIED, mode, 0, menuSlot, "", ComparisonMode.ITEM_ID, "");
    }

    public static BackpackTransferPayload externalToCarried(int containerId, ItemIdentity identity, Mode mode) {
        ItemIdentity resolvedIdentity = identity;
        return new BackpackTransferPayload(
                containerId,
                Direction.EXTERNAL_TO_CARRIED,
                mode,
                0,
                -1,
                resolvedIdentity == null ? "" : resolvedIdentity.itemId(),
                resolvedIdentity == null ? ComparisonMode.ITEM_ID : resolvedIdentity.comparisonMode(),
                resolvedIdentity == null ? "" : resolvedIdentity.componentFingerprint()
        );
    }

    public static BackpackTransferPayload externalToCarried(int containerId, ItemIdentity identity, int requestedCount) {
        ItemIdentity resolvedIdentity = identity;
        return new BackpackTransferPayload(
                containerId,
                Direction.EXTERNAL_TO_CARRIED,
                Mode.STACK,
                requestedCount,
                -1,
                resolvedIdentity == null ? "" : resolvedIdentity.itemId(),
                resolvedIdentity == null ? ComparisonMode.ITEM_ID : resolvedIdentity.comparisonMode(),
                resolvedIdentity == null ? "" : resolvedIdentity.componentFingerprint()
        );
    }

    public static BackpackTransferPayload menuToExternal(int containerId, int menuSlot, Mode mode) {
        return new BackpackTransferPayload(containerId, Direction.MENU_TO_EXTERNAL, mode, 0, menuSlot, "", ComparisonMode.ITEM_ID, "");
    }

    public static BackpackTransferPayload menuToExternal(int containerId, MenuSlotId menuSlot, Mode mode) {
        return menuToExternal(containerId, menuSlot == null ? -1 : menuSlot.orElse(-1), mode);
    }

    public static BackpackTransferPayload carriedToExternal(int containerId, ItemIdentity identity, int requestedCount) {
        ItemIdentity resolvedIdentity = identity;
        return new BackpackTransferPayload(
                containerId,
                Direction.CARRIED_TO_EXTERNAL,
                Mode.STACK,
                requestedCount,
                -1,
                resolvedIdentity == null ? "" : resolvedIdentity.itemId(),
                resolvedIdentity == null ? ComparisonMode.ITEM_ID : resolvedIdentity.comparisonMode(),
                resolvedIdentity == null ? "" : resolvedIdentity.componentFingerprint()
        );
    }

    public static BackpackTransferPayload backpackToExternal(int containerId, ItemIdentity identity, Mode mode) {
        ItemIdentity resolvedIdentity = identity;
        return new BackpackTransferPayload(
                containerId,
                Direction.BACKPACK_TO_EXTERNAL,
                mode,
                0,
                -1,
                resolvedIdentity == null ? "" : resolvedIdentity.itemId(),
                resolvedIdentity == null ? ComparisonMode.ITEM_ID : resolvedIdentity.comparisonMode(),
                resolvedIdentity == null ? "" : resolvedIdentity.componentFingerprint()
        );
    }

    public static BackpackTransferPayload backpackToMenu(int containerId, ItemIdentity identity, int menuSlot) {
        return backpackToMenu(containerId, identity, menuSlot, 0);
    }

    public static BackpackTransferPayload backpackToMenu(int containerId, ItemIdentity identity, MenuSlotId menuSlot) {
        return backpackToMenu(containerId, identity, menuSlot, 0);
    }

    public static BackpackTransferPayload backpackToMenu(int containerId, ItemIdentity identity, int menuSlot, int requestedCount) {
        ItemIdentity resolvedIdentity = identity;
        return new BackpackTransferPayload(
                containerId,
                Direction.BACKPACK_TO_MENU,
                Mode.STACK,
                requestedCount,
                menuSlot,
                resolvedIdentity == null ? "" : resolvedIdentity.itemId(),
                resolvedIdentity == null ? ComparisonMode.ITEM_ID : resolvedIdentity.comparisonMode(),
                resolvedIdentity == null ? "" : resolvedIdentity.componentFingerprint()
        );
    }

    public static BackpackTransferPayload backpackToMenu(int containerId, ItemIdentity identity, MenuSlotId menuSlot, int requestedCount) {
        return backpackToMenu(containerId, identity, menuSlot == null ? -1 : menuSlot.orElse(-1), requestedCount);
    }

    public ItemIdentity identity() {
        if (itemId == null || itemId.isBlank()) {
            return null;
        }
        return new ItemIdentity(itemId, comparisonMode, componentFingerprint);
    }

    public MenuSlotId menuSlotId() {
        return MenuSlotId.of(menuSlot);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public enum Direction {
        EXTERNAL_TO_CARRIED,
        MENU_TO_EXTERNAL,
        CARRIED_TO_EXTERNAL,
        BACKPACK_TO_EXTERNAL,
        BACKPACK_TO_MENU
    }

    public enum Mode {
        ONE,
        STACK,
        ALL
    }
}
