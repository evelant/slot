package dev.imagio.slot.network;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.client.model.ComparisonMode;
import dev.imagio.slot.client.model.ItemIdentity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record CursorTransferPayload(
        int containerId,
        Action action,
        TargetPane targetPane,
        Mode mode,
        int targetMenuSlot,
        String itemId,
        ComparisonMode comparisonMode,
        String componentFingerprint
) implements CustomPacketPayload {
    public static final Type<CursorTransferPayload> TYPE = new Type<>(SlotCommon.id("cursor_transfer"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CursorTransferPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public CursorTransferPayload decode(RegistryFriendlyByteBuf buf) {
            return new CursorTransferPayload(
                    buf.readVarInt(),
                    buf.readEnum(Action.class),
                    buf.readEnum(TargetPane.class),
                    buf.readEnum(Mode.class),
                    buf.readVarInt(),
                    buf.readUtf(),
                    buf.readEnum(ComparisonMode.class),
                    buf.readUtf()
            );
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, CursorTransferPayload payload) {
            buf.writeVarInt(payload.containerId);
            buf.writeEnum(payload.action);
            buf.writeEnum(payload.targetPane);
            buf.writeEnum(payload.mode);
            buf.writeVarInt(payload.targetMenuSlot);
            buf.writeUtf(payload.itemId);
            buf.writeEnum(payload.comparisonMode);
            buf.writeUtf(payload.componentFingerprint);
        }
    };

    public CursorTransferPayload {
        action = action == null ? Action.DROP_CARRIED : action;
        targetPane = targetPane == null ? TargetPane.CARRIED : targetPane;
        mode = mode == null ? Mode.STACK : mode;
        targetMenuSlot = Math.max(-1, targetMenuSlot);
        itemId = itemId == null ? "" : itemId;
        comparisonMode = comparisonMode == null ? ComparisonMode.ITEM_ID : comparisonMode;
        componentFingerprint = componentFingerprint == null ? "" : componentFingerprint;
    }

    public static CursorTransferPayload pickupMatching(int containerId, TargetPane targetPane, Mode mode, ItemIdentity identity) {
        ItemIdentity resolvedIdentity = identity;
        return new CursorTransferPayload(
                containerId,
                Action.PICKUP_MATCHING,
                targetPane,
                mode,
                -1,
                resolvedIdentity == null ? "" : resolvedIdentity.itemId(),
                resolvedIdentity == null ? ComparisonMode.ITEM_ID : resolvedIdentity.comparisonMode(),
                resolvedIdentity == null ? "" : resolvedIdentity.componentFingerprint()
        );
    }

    public static CursorTransferPayload dropCarried(int containerId, TargetPane targetPane, Mode mode) {
        return new CursorTransferPayload(
                containerId,
                Action.DROP_CARRIED,
                targetPane,
                mode,
                -1,
                "",
                ComparisonMode.ITEM_ID,
                ""
        );
    }

    public static CursorTransferPayload dropCarriedToSlot(int containerId, int targetMenuSlot, Mode mode) {
        return new CursorTransferPayload(
                containerId,
                Action.DROP_CARRIED_TO_SLOT,
                TargetPane.CARRIED,
                mode,
                targetMenuSlot,
                "",
                ComparisonMode.ITEM_ID,
                ""
        );
    }

    public static CursorTransferPayload trashCarried(int containerId, Mode mode) {
        return new CursorTransferPayload(
                containerId,
                Action.TRASH_CARRIED,
                TargetPane.CARRIED,
                mode,
                -1,
                "",
                ComparisonMode.ITEM_ID,
                ""
        );
    }

    public static CursorTransferPayload voidMatchingCarried(int containerId, ItemIdentity identity) {
        return voidMatchingCarried(containerId, identity, Mode.ONE);
    }

    public static CursorTransferPayload voidMatchingCarried(int containerId, ItemIdentity identity, Mode mode) {
        ItemIdentity resolvedIdentity = identity;
        return new CursorTransferPayload(
                containerId,
                Action.VOID_MATCHING_CARRIED,
                TargetPane.CARRIED,
                mode,
                -1,
                resolvedIdentity == null ? "" : resolvedIdentity.itemId(),
                resolvedIdentity == null ? ComparisonMode.ITEM_ID : resolvedIdentity.comparisonMode(),
                resolvedIdentity == null ? "" : resolvedIdentity.componentFingerprint()
        );
    }

    public ItemIdentity identity() {
        if (itemId == null || itemId.isBlank()) {
            return null;
        }
        return new ItemIdentity(itemId, comparisonMode, componentFingerprint);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public enum Action {
        PICKUP_MATCHING,
        DROP_CARRIED,
        DROP_CARRIED_TO_SLOT,
        TRASH_CARRIED,
        VOID_MATCHING_CARRIED
    }

    public enum TargetPane {
        OPEN_CONTAINER,
        CARRIED
    }

    public enum Mode {
        ONE,
        HALF,
        STACK
    }
}
