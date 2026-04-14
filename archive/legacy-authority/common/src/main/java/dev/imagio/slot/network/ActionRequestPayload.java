package dev.imagio.slot.network;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.intent.ActionRequest;
import dev.imagio.slot.intent.ActionRequestId;
import dev.imagio.slot.source.SourceId;
import dev.imagio.slot.source.SourceSlotRef;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ActionRequestPayload(ActionRequest request) implements CustomPacketPayload {
    public static final Type<ActionRequestPayload> TYPE = new Type<>(SlotCommon.id("action_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ActionRequestPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ActionRequestPayload decode(RegistryFriendlyByteBuf buf) {
            ActionRequest request = new ActionRequest(
                    buf.readVarInt(),
                    new ActionRequestId(buf.readUtf()),
                    buf.readUtf(),
                    buf.readVarInt(),
                    buf.readEnum(dev.imagio.slot.intent.ActionFamily.class),
                    decodeRef(buf),
                    decodeRef(buf),
                    buf.readUtf(),
                    buf.readUtf(),
                    buf.readVarInt()
            );
            return new ActionRequestPayload(request);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, ActionRequestPayload payload) {
            ActionRequest request = payload == null ? null : payload.request();
            if (request == null) {
                request = ActionRequest.forSession(dev.imagio.slot.intent.ActionFamily.TRANSFER, "", -1);
            }

            buf.writeVarInt(request.requestSchemaVersion());
            buf.writeUtf(request.requestId().value());
            buf.writeUtf(request.expectedSessionFingerprint());
            buf.writeVarInt(request.expectedContainerId());
            buf.writeEnum(request.actionFamily());
            encodeRef(buf, request.primarySourceRef());
            encodeRef(buf, request.secondarySourceRef());
            buf.writeUtf(request.toolRef());
            buf.writeUtf(request.identityKey());
            buf.writeVarInt(request.requestedCount());
        }

        private void encodeRef(RegistryFriendlyByteBuf buf, SourceSlotRef ref) {
            buf.writeBoolean(ref != null);
            if (ref == null) {
                return;
            }

            buf.writeUtf(ref.kind());
            buf.writeUtf(ref.sourceId().value());
            buf.writeUtf(ref.payload());
        }

        private SourceSlotRef decodeRef(RegistryFriendlyByteBuf buf) {
            if (!buf.readBoolean()) {
                return null;
            }
            return new SourceSlotRef(
                    buf.readUtf(),
                    SourceId.of(buf.readUtf()),
                    buf.readUtf()
            );
        }
    };

    public ActionRequestPayload {
        request = request == null ? ActionRequest.forSession(dev.imagio.slot.intent.ActionFamily.TRANSFER, "", -1) : request;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
