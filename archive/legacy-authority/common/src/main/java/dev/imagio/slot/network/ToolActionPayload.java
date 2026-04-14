package dev.imagio.slot.network;

import dev.imagio.slot.SlotCommon;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ToolActionPayload(
        int containerId,
        String toolId,
        Action action
) implements CustomPacketPayload {
    public static final Type<ToolActionPayload> TYPE = new Type<>(SlotCommon.id("tool_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ToolActionPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ToolActionPayload decode(RegistryFriendlyByteBuf buf) {
            return new ToolActionPayload(
                    buf.readVarInt(),
                    buf.readUtf(),
                    buf.readEnum(Action.class)
            );
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, ToolActionPayload payload) {
            buf.writeVarInt(payload.containerId);
            buf.writeUtf(payload.toolId);
            buf.writeEnum(payload.action);
        }
    };

    public ToolActionPayload {
        toolId = toolId == null ? "" : toolId;
        action = action == null ? Action.CLEAR_GRID : action;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public enum Action {
        CLEAR_GRID,
        BALANCE_GRID,
        ROTATE_GRID_CW,
        ROTATE_GRID_CCW,
        TOGGLE_AUTO_REFILL
    }
}
