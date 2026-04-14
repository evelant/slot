package dev.imagio.slot.network;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.intent.ActionFamily;
import dev.imagio.slot.intent.ActionRequestId;
import dev.imagio.slot.operation.ActionOutcome;
import dev.imagio.slot.operation.ActionReason;
import dev.imagio.slot.operation.ActionStatus;
import dev.imagio.slot.operation.RefreshScope;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.ArrayList;
import java.util.List;

public record SlotActionOutcomePayload(
        String menuKey,
        ActionRequestId requestId,
        ActionFamily actionFamily,
        ActionStatus status,
        ActionReason reason,
        RefreshScope refreshScope,
        int affectedCount,
        List<String> acquisitionItemIds,
        String acquisitionProducerId,
        String summaryKey
) implements CustomPacketPayload {
    public static final Type<SlotActionOutcomePayload> TYPE = new Type<>(SlotCommon.id("action_outcome"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SlotActionOutcomePayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public SlotActionOutcomePayload decode(RegistryFriendlyByteBuf buf) {
            return new SlotActionOutcomePayload(
                    buf.readUtf(),
                    new ActionRequestId(buf.readUtf()),
                    buf.readEnum(ActionFamily.class),
                    buf.readEnum(ActionStatus.class),
                    buf.readEnum(ActionReason.class),
                    buf.readEnum(RefreshScope.class),
                    buf.readVarInt(),
                    readList(buf),
                    buf.readUtf(),
                    buf.readUtf()
            );
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, SlotActionOutcomePayload payload) {
            buf.writeUtf(payload.menuKey);
            buf.writeUtf(payload.requestId.value());
            buf.writeEnum(payload.actionFamily);
            buf.writeEnum(payload.status);
            buf.writeEnum(payload.reason);
            buf.writeEnum(payload.refreshScope);
            buf.writeVarInt(payload.affectedCount);
            writeList(buf, payload.acquisitionItemIds);
            buf.writeUtf(payload.acquisitionProducerId);
            buf.writeUtf(payload.summaryKey);
        }
    };

    public SlotActionOutcomePayload {
        menuKey = menuKey == null ? "" : menuKey;
        requestId = requestId == null ? ActionRequestId.none() : requestId;
        actionFamily = actionFamily == null ? ActionFamily.TRANSFER : actionFamily;
        status = status == null ? ActionStatus.BLOCKED : status;
        reason = reason == null ? (status == ActionStatus.CONFIRMED ? ActionReason.NONE : ActionReason.UNSPECIFIED) : reason;
        refreshScope = refreshScope == null ? RefreshScope.NONE : refreshScope;
        affectedCount = Math.max(0, affectedCount);
        acquisitionItemIds = acquisitionItemIds == null ? List.of() : List.copyOf(acquisitionItemIds);
        acquisitionProducerId = acquisitionProducerId == null ? "" : acquisitionProducerId;
        summaryKey = summaryKey == null ? "" : summaryKey;
    }

    public static SlotActionOutcomePayload from(String menuKey, ActionOutcome outcome) {
        ActionOutcome resolved = outcome == null
                ? ActionOutcome.blocked(ActionRequestId.none(), ActionFamily.TRANSFER, ActionReason.UNSPECIFIED, RefreshScope.NONE)
                : outcome;
        return new SlotActionOutcomePayload(
                menuKey,
                resolved.requestId(),
                resolved.actionFamily(),
                resolved.status(),
                resolved.reason(),
                resolved.refreshScope(),
                resolved.affectedCount(),
                resolved.acquisitionItemIds(),
                resolved.acquisitionProducerId(),
                resolved.summaryKey()
        );
    }

    public static SlotActionOutcomePayload confirmed(String menuKey, ActionFamily actionFamily, int affectedCount) {
        return from(menuKey, ActionOutcome.confirmed(ActionRequestId.none(), actionFamily, affectedCount, RefreshScope.SESSION));
    }

    public static SlotActionOutcomePayload blocked(String menuKey, ActionFamily actionFamily) {
        return from(menuKey, ActionOutcome.blocked(ActionRequestId.none(), actionFamily, ActionReason.UNSPECIFIED, RefreshScope.SESSION));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private static List<String> readList(RegistryFriendlyByteBuf buf) {
        int size = Math.max(0, buf.readVarInt());
        List<String> values = new ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            values.add(buf.readUtf());
        }
        return List.copyOf(values);
    }

    private static void writeList(RegistryFriendlyByteBuf buf, List<String> values) {
        List<String> safeValues = values == null ? List.of() : values;
        buf.writeVarInt(safeValues.size());
        for (String value : safeValues) {
            buf.writeUtf(value == null ? "" : value);
        }
    }
}
