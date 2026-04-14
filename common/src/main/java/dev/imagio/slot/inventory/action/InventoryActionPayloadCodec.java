package dev.imagio.slot.inventory.action;

import dev.imagio.slot.inventory.core.ItemComparisonMode;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.HostInstanceKey;
import dev.imagio.slot.inventory.core.ServerMenuRef;
import dev.imagio.slot.workflow.domain.InventoryActivityConfidence;
import dev.imagio.slot.workflow.domain.InventoryActivityEvent;
import dev.imagio.slot.workflow.domain.InventoryActivityKind;
import dev.imagio.slot.workflow.domain.InventoryActivityProducer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

final class InventoryActionPayloadCodec {
    private InventoryActionPayloadCodec() {
    }

    static HostInstanceKey readOptionalHostKey(RegistryFriendlyByteBuf buf) {
        if (!buf.readBoolean()) {
            return null;
        }
        return new HostInstanceKey(
                buf.readUtf(),
                buf.readInt(),
                buf.readUtf(),
                buf.readUtf()
        );
    }

    static void writeOptionalHostKey(RegistryFriendlyByteBuf buf, HostInstanceKey value) {
        buf.writeBoolean(value != null);
        if (value != null) {
            buf.writeUtf(value.menuClass());
            buf.writeInt(value.containerId());
            buf.writeUtf(value.providerId());
            buf.writeUtf(value.providerScopeId());
        }
    }

    static ServerMenuRef readServerMenuRef(RegistryFriendlyByteBuf buf) {
        return new ServerMenuRef(buf.readUtf(), buf.readInt());
    }

    static void writeServerMenuRef(RegistryFriendlyByteBuf buf, ServerMenuRef value) {
        ServerMenuRef ref = value == null ? new ServerMenuRef("", -1) : value;
        buf.writeUtf(ref.menuClassName());
        buf.writeInt(ref.containerId());
    }

    static ItemIdentity readOptionalIdentity(RegistryFriendlyByteBuf buf) {
        if (!buf.readBoolean()) {
            return null;
        }
        return new ItemIdentity(
                buf.readUtf(),
                buf.readEnum(ItemComparisonMode.class),
                buf.readUtf()
        );
    }

    static void writeOptionalIdentity(RegistryFriendlyByteBuf buf, ItemIdentity value) {
        buf.writeBoolean(value != null);
        if (value != null) {
            buf.writeUtf(value.itemId());
            buf.writeEnum(value.comparisonMode());
            buf.writeUtf(value.componentFingerprint());
        }
    }

    static java.util.List<InventoryActivityEvent> readActivityEvents(RegistryFriendlyByteBuf buf) {
        int size = Math.max(0, buf.readInt());
        java.util.ArrayList<InventoryActivityEvent> events = new java.util.ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            events.add(new InventoryActivityEvent(
                    buf.readEnum(InventoryActivityKind.class),
                    buf.readEnum(InventoryActivityProducer.class),
                    buf.readEnum(InventoryActivityConfidence.class),
                    readOptionalIdentity(buf),
                    buf.readInt(),
                    readOptionalTarget(buf),
                    readOptionalTarget(buf),
                    buf.readUtf(),
                    buf.readUtf(),
                    readReasonCodes(buf),
                    buf.readUtf()
            ));
        }
        return java.util.List.copyOf(events);
    }

    static void writeActivityEvents(RegistryFriendlyByteBuf buf, java.util.List<InventoryActivityEvent> events) {
        java.util.List<InventoryActivityEvent> resolved = events == null
                ? java.util.List.of()
                : events.stream().filter(InventoryActivityEvent::present).toList();
        buf.writeInt(resolved.size());
        for (InventoryActivityEvent event : resolved) {
            buf.writeEnum(event.kind());
            buf.writeEnum(event.producer());
            buf.writeEnum(event.confidence());
            writeOptionalIdentity(buf, event.identity());
            buf.writeInt(event.count());
            writeOptionalTarget(buf, event.fromTarget());
            writeOptionalTarget(buf, event.toTarget());
            buf.writeUtf(event.requestId());
            buf.writeUtf(event.recoveryToken());
            writeReasonCodes(buf, event.reasonCodes());
            buf.writeUtf(event.diagnostics());
        }
    }

    static java.util.List<InventoryCommandReasonCode> readReasonCodes(RegistryFriendlyByteBuf buf) {
        int size = Math.max(0, buf.readInt());
        java.util.ArrayList<InventoryCommandReasonCode> reasonCodes = new java.util.ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            reasonCodes.add(buf.readEnum(InventoryCommandReasonCode.class));
        }
        return java.util.List.copyOf(reasonCodes);
    }

    static void writeReasonCodes(RegistryFriendlyByteBuf buf, java.util.List<InventoryCommandReasonCode> reasonCodes) {
        java.util.List<InventoryCommandReasonCode> resolved = reasonCodes == null
                ? java.util.List.of()
                : reasonCodes.stream().filter(java.util.Objects::nonNull).distinct().toList();
        buf.writeInt(resolved.size());
        for (InventoryCommandReasonCode reasonCode : resolved) {
            buf.writeEnum(reasonCode);
        }
    }

    static ItemStack readStack(RegistryFriendlyByteBuf buf) {
        return buf == null ? ItemStack.EMPTY : ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
    }

    static void writeStack(RegistryFriendlyByteBuf buf, ItemStack stack) {
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, stack == null ? ItemStack.EMPTY : stack);
    }

    static InventoryActionTarget readOptionalTarget(RegistryFriendlyByteBuf buf) {
        if (!buf.readBoolean()) {
            return null;
        }
        return switch (buf.readEnum(TargetType.class)) {
            case CURSOR -> new InventoryActionTarget.CursorTarget();
            case SOURCE -> new InventoryActionTarget.SourceTarget(buf.readUtf());
            case SOURCE_SLOT -> new InventoryActionTarget.SourceSlotTarget(buf.readUtf(), buf.readInt());
            case SOURCE_ENTRY -> new InventoryActionTarget.SourceEntryTarget(buf.readUtf(), buf.readUtf());
            case QUICK_ACCESS -> new InventoryActionTarget.QuickAccessTarget(buf.readUtf(), buf.readInt());
            case EQUIPMENT -> new InventoryActionTarget.EquipmentTarget(buf.readUtf(), buf.readInt());
            case TOOL_REGION -> new InventoryActionTarget.ToolRegionTarget(buf.readUtf(), buf.readUtf(), buf.readInt());
            case TOOL_CONTROL -> new InventoryActionTarget.ToolControlTarget(buf.readUtf(), buf.readUtf());
        };
    }

    static void writeOptionalTarget(RegistryFriendlyByteBuf buf, InventoryActionTarget target) {
        buf.writeBoolean(target != null);
        if (target == null) {
            return;
        }

        switch (target) {
            case InventoryActionTarget.CursorTarget ignored -> buf.writeEnum(TargetType.CURSOR);
            case InventoryActionTarget.SourceTarget sourceTarget -> {
                buf.writeEnum(TargetType.SOURCE);
                buf.writeUtf(sourceTarget.sourceId());
            }
            case InventoryActionTarget.SourceSlotTarget sourceSlotTarget -> {
                buf.writeEnum(TargetType.SOURCE_SLOT);
                buf.writeUtf(sourceSlotTarget.sourceId());
                buf.writeInt(sourceSlotTarget.slotIndex());
            }
            case InventoryActionTarget.SourceEntryTarget sourceEntryTarget -> {
                buf.writeEnum(TargetType.SOURCE_ENTRY);
                buf.writeUtf(sourceEntryTarget.sourceId());
                buf.writeUtf(sourceEntryTarget.entryId());
            }
            case InventoryActionTarget.QuickAccessTarget quickAccessTarget -> {
                buf.writeEnum(TargetType.QUICK_ACCESS);
                buf.writeUtf(quickAccessTarget.laneId());
                buf.writeInt(quickAccessTarget.slotIndex());
            }
            case InventoryActionTarget.EquipmentTarget equipmentTarget -> {
                buf.writeEnum(TargetType.EQUIPMENT);
                buf.writeUtf(equipmentTarget.groupId());
                buf.writeInt(equipmentTarget.slotIndex());
            }
            case InventoryActionTarget.ToolRegionTarget toolRegionTarget -> {
                buf.writeEnum(TargetType.TOOL_REGION);
                buf.writeUtf(toolRegionTarget.toolId());
                buf.writeUtf(toolRegionTarget.regionId());
                buf.writeInt(toolRegionTarget.slotIndex());
            }
            case InventoryActionTarget.ToolControlTarget toolControlTarget -> {
                buf.writeEnum(TargetType.TOOL_CONTROL);
                buf.writeUtf(toolControlTarget.toolId());
                buf.writeUtf(toolControlTarget.controlId());
            }
        }
    }

    private enum TargetType {
        CURSOR,
        SOURCE,
        SOURCE_SLOT,
        SOURCE_ENTRY,
        QUICK_ACCESS,
        EQUIPMENT,
        TOOL_REGION,
        TOOL_CONTROL
    }
}
