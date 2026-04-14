package dev.imagio.slot.inventory.action;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.inventory.core.InventoryToolActionId;
import dev.imagio.slot.inventory.core.InventoryToolToggleId;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;

public record InventoryActionRequestPayload(InventoryActionRequest request) implements CustomPacketPayload {
    public static final Type<InventoryActionRequestPayload> TYPE = new Type<>(SlotCommon.id("inventory_action_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf, InventoryActionRequestPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public InventoryActionRequestPayload decode(RegistryFriendlyByteBuf buf) {
            return new InventoryActionRequestPayload(new InventoryActionRequest(
                    InventoryActionPayloadCodec.readOptionalHostKey(buf),
                    InventoryActionPayloadCodec.readServerMenuRef(buf),
                    buf.readUtf(),
                    buf.readEnum(InventoryActionKind.class),
                    buf.readEnum(InventoryActionMode.class),
                    buf.readUtf(),
                    buf.readUtf(),
                    buf.readUtf(),
                    buf.readUtf(),
                    InventoryActionPayloadCodec.readOptionalTarget(buf),
                    InventoryActionPayloadCodec.readOptionalTarget(buf),
                    buf.readInt(),
                    InventoryActionPayloadCodec.readOptionalIdentity(buf),
                    InventoryActionPayloadCodec.readStack(buf),
                    buf.readEnum(InventoryToolActionId.class),
                    buf.readEnum(InventoryToolToggleId.class),
                    buf.readBoolean(),
                    buf.readUtf()
            ));
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, InventoryActionRequestPayload payload) {
            InventoryActionRequest request = payload == null ? null : payload.request();
            if (request == null) {
                request = new InventoryActionRequest(
                        null,
                        null,
                        "",
                        InventoryActionKind.TRANSFER_ONE,
                        InventoryActionMode.EXECUTE,
                        "",
                        "",
                        "",
                        "",
                        null,
                        null,
                        0,
                        null,
                        ItemStack.EMPTY,
                        InventoryToolActionId.PROVIDER_DEFINED,
                        InventoryToolToggleId.PROVIDER_DEFINED,
                        false,
                        ""
                );
            }

            InventoryActionPayloadCodec.writeOptionalHostKey(buf, request.hostId());
            InventoryActionPayloadCodec.writeServerMenuRef(buf, request.serverMenuRef());
            buf.writeUtf(request.requestId());
            buf.writeEnum(request.kind());
            buf.writeEnum(request.mode());
            buf.writeUtf(request.origin());
            buf.writeUtf(request.correlationId());
            buf.writeUtf(request.causationId());
            buf.writeUtf(request.sessionId());
            InventoryActionPayloadCodec.writeOptionalTarget(buf, request.primaryTarget());
            InventoryActionPayloadCodec.writeOptionalTarget(buf, request.secondaryTarget());
            buf.writeInt(request.requestedCount());
            InventoryActionPayloadCodec.writeOptionalIdentity(buf, request.identity());
            InventoryActionPayloadCodec.writeStack(buf, request.stack());
            buf.writeEnum(request.toolActionId() == null ? InventoryToolActionId.PROVIDER_DEFINED : request.toolActionId());
            buf.writeEnum(request.toolToggleId() == null ? InventoryToolToggleId.PROVIDER_DEFINED : request.toolToggleId());
            buf.writeBoolean(request.desiredToggleState());
            buf.writeUtf(request.diagnostics());
        }
    };

    public InventoryActionRequestPayload {
        request = request == null
                ? new InventoryActionRequest(
                null,
                null,
                "",
                InventoryActionKind.TRANSFER_ONE,
                InventoryActionMode.EXECUTE,
                "",
                "",
                "",
                "",
                null,
                null,
                0,
                null,
                ItemStack.EMPTY,
                InventoryToolActionId.PROVIDER_DEFINED,
                InventoryToolToggleId.PROVIDER_DEFINED,
                false,
                ""
        )
                : request;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
