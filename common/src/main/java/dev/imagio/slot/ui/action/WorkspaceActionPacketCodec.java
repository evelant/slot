package dev.imagio.slot.ui.action;

import java.util.ArrayList;

public final class WorkspaceActionPacketCodec {
    private WorkspaceActionPacketCodec() {
    }

    public static void write(WorkspaceActionPacketBuffer buffer, WorkspaceActionPacket packet) {
        if (buffer == null) {
            throw new IllegalArgumentException("buffer must not be null");
        }
        if (packet == null) {
            throw new IllegalArgumentException("packet must not be null");
        }
        WorkspaceActionValidation validation = packet.validateShape();
        if (!validation.valid()) {
            throw new IllegalArgumentException(validation.diagnostics());
        }
        WorkspaceActionEnvelope envelope = packet.envelope();
        if (envelope == null) {
            envelope = new WorkspaceActionEnvelope("", WorkspaceActionEnvelope.NO_MENU_CONTAINER, 0L);
        }
        buffer.writeString(envelope.sessionId());
        buffer.writeInt(envelope.menuContainerId());
        buffer.writeLong(envelope.viewRevision());
        buffer.writeString(WorkspaceActionCatalog.require(packet.action()).wireId());
        buffer.writeInt(packet.arguments().size());
        for (WorkspaceActionArgumentValue argument : packet.arguments()) {
            buffer.writeBoolean(argument.isNull());
            if (argument.isNull()) {
                continue;
            }
            switch (argument.type()) {
                case STRING -> buffer.writeString((String) argument.toObject());
                case INTEGER -> buffer.writeInt((Integer) argument.toObject());
                case DOUBLE -> buffer.writeDouble((Double) argument.toObject());
            }
        }
    }

    public static WorkspaceActionPacket read(WorkspaceActionPacketBuffer buffer) {
        if (buffer == null) {
            throw new IllegalArgumentException("buffer must not be null");
        }
        WorkspaceActionEnvelope envelope = new WorkspaceActionEnvelope(
                buffer.readString(),
                buffer.readInt(),
                buffer.readLong()
        );
        String wireId = buffer.readString();
        WorkspaceActionDefinition definition = WorkspaceActionCatalog.byWireId(wireId)
                .orElseThrow(() -> new IllegalArgumentException("unknown_action:" + wireId));
        int argumentCount = buffer.readInt();
        if (argumentCount != definition.argumentTypes().size()) {
            throw new IllegalArgumentException(
                    "argument_count_mismatch:expected=" + definition.argumentTypes().size()
                            + ":actual=" + argumentCount
            );
        }
        ArrayList<WorkspaceActionArgumentValue> arguments = new ArrayList<>(argumentCount);
        for (int index = 0; index < argumentCount; index++) {
            WorkspaceActionArgumentType type = definition.argumentTypes().get(index);
            boolean isNull = buffer.readBoolean();
            if (isNull) {
                arguments.add(WorkspaceActionArgumentValue.of(type, null));
                continue;
            }
            Object value = switch (type) {
                case STRING -> buffer.readString();
                case INTEGER -> buffer.readInt();
                case DOUBLE -> buffer.readDouble();
            };
            arguments.add(WorkspaceActionArgumentValue.of(type, value));
        }
        WorkspaceActionPacket packet = new WorkspaceActionPacket(envelope, definition.action(), arguments);
        WorkspaceActionValidation validation = packet.validateShape();
        if (!validation.valid()) {
            throw new IllegalArgumentException(validation.diagnostics());
        }
        return packet;
    }
}
