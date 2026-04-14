package dev.imagio.slot.inventory.action;

import dev.imagio.slot.SlotCommon;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;

public record InventoryActionOutcomePayload(InventoryActionOutcome outcome) implements CustomPacketPayload {
    public static final Type<InventoryActionOutcomePayload> TYPE = new Type<>(SlotCommon.id("inventory_action_outcome"));
    public static final StreamCodec<RegistryFriendlyByteBuf, InventoryActionOutcomePayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public InventoryActionOutcomePayload decode(RegistryFriendlyByteBuf buf) {
            return new InventoryActionOutcomePayload(new InventoryActionOutcome(
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
                    buf.readEnum(InventoryActionStatus.class),
                    InventoryActionPayloadCodec.readReasonCodes(buf),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readBoolean(),
                    InventoryActionPayloadCodec.readActivityEvents(buf),
                    InventoryActionPayloadCodec.readStack(buf),
                    buf.readUtf()
            ));
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, InventoryActionOutcomePayload payload) {
            InventoryActionOutcome outcome = payload == null ? null : payload.outcome();
            if (outcome == null) {
                outcome = new InventoryActionOutcome(
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
                        InventoryActionStatus.FAILED,
                        java.util.List.of(),
                        0,
                        0,
                        false,
                        java.util.List.of(),
                        ItemStack.EMPTY,
                        ""
                );
            }

            InventoryActionPayloadCodec.writeOptionalHostKey(buf, outcome.hostId());
            InventoryActionPayloadCodec.writeServerMenuRef(buf, outcome.serverMenuRef());
            buf.writeUtf(outcome.requestId());
            buf.writeEnum(outcome.kind());
            buf.writeEnum(outcome.mode());
            buf.writeUtf(outcome.origin());
            buf.writeUtf(outcome.correlationId());
            buf.writeUtf(outcome.causationId());
            buf.writeUtf(outcome.sessionId());
            InventoryActionPayloadCodec.writeOptionalTarget(buf, outcome.primaryTarget());
            InventoryActionPayloadCodec.writeOptionalTarget(buf, outcome.secondaryTarget());
            buf.writeEnum(outcome.status());
            InventoryActionPayloadCodec.writeReasonCodes(buf, outcome.reasonCodes());
            buf.writeInt(outcome.requestedCount());
            buf.writeInt(outcome.appliedCount());
            buf.writeBoolean(outcome.capacityUncertain());
            InventoryActionPayloadCodec.writeActivityEvents(buf, outcome.activityEvents());
            InventoryActionPayloadCodec.writeStack(buf, outcome.stackRemainder());
            buf.writeUtf(outcome.diagnostics());
        }
    };

    public InventoryActionOutcomePayload {
        outcome = outcome == null
                ? new InventoryActionOutcome(
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
                InventoryActionStatus.FAILED,
                java.util.List.of(),
                0,
                0,
                false,
                java.util.List.of(),
                ItemStack.EMPTY,
                ""
        )
                : outcome;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
