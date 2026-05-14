package dev.imagio.slot.ui.action;

import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkspaceActionCatalogTest {
    @Test
    void exposesEveryDeclaredActionExactlyOnce() {
        assertEquals(WorkspaceActionId.values().length, WorkspaceActionCatalog.definitions().size());

        HashSet<WorkspaceActionId> actions = new HashSet<>();
        HashSet<String> wireIds = new HashSet<>();
        for (WorkspaceActionDefinition definition : WorkspaceActionCatalog.definitions()) {
            assertTrue(actions.add(definition.action()), "duplicate action " + definition.action());
            assertTrue(wireIds.add(definition.wireId()), "duplicate wire id " + definition.wireId());
            assertFalse(definition.wireId().isBlank());
            assertSame(definition, WorkspaceActionCatalog.require(definition.action()));
            assertSame(definition, WorkspaceActionCatalog.byWireId(definition.wireId()).orElseThrow());
        }
    }

    @Test
    void keySchemasMatchCurrentWorkspaceRpcSurface() {
        assertEquals(
                java.util.List.of(
                        WorkspaceActionArgumentType.INTEGER,
                        WorkspaceActionArgumentType.INTEGER,
                        WorkspaceActionArgumentType.INTEGER,
                        WorkspaceActionArgumentType.INTEGER,
                        WorkspaceActionArgumentType.STRING
                ),
                WorkspaceActionCatalog.require(WorkspaceActionId.TRANSFER).argumentTypes()
        );
        assertEquals(
                java.util.List.of(
                        WorkspaceActionArgumentType.STRING,
                        WorkspaceActionArgumentType.STRING,
                        WorkspaceActionArgumentType.STRING
                ),
                WorkspaceActionCatalog.require(WorkspaceActionId.TAKE_DESIRED_GAP_OR_STACK_BY_IDENTITY).argumentTypes()
        );
        assertEquals(
                java.util.List.of(
                        WorkspaceActionArgumentType.STRING,
                        WorkspaceActionArgumentType.STRING,
                        WorkspaceActionArgumentType.STRING,
                        WorkspaceActionArgumentType.INTEGER
                ),
                WorkspaceActionCatalog.require(WorkspaceActionId.PICKUP_TO_CURSOR).argumentTypes()
        );
        assertEquals(
                java.util.List.of(
                        WorkspaceActionArgumentType.STRING,
                        WorkspaceActionArgumentType.STRING,
                        WorkspaceActionArgumentType.STRING,
                        WorkspaceActionArgumentType.INTEGER
                ),
                WorkspaceActionCatalog.require(WorkspaceActionId.TAKE_ITEMS_BY_IDENTITY).argumentTypes()
        );
        assertEquals(
                java.util.List.of(
                        WorkspaceActionArgumentType.STRING,
                        WorkspaceActionArgumentType.STRING,
                        WorkspaceActionArgumentType.STRING,
                        WorkspaceActionArgumentType.INTEGER
                ),
                WorkspaceActionCatalog.require(WorkspaceActionId.DEPOSIT_ITEMS_HOME_TO_LINKED_CHEST).argumentTypes()
        );
        assertEquals(
                java.util.List.of(),
                WorkspaceActionCatalog.require(WorkspaceActionId.CURSOR_CANCEL).argumentTypes()
        );
    }

    @Test
    void validatorAcceptsNullOptionalArgumentsButRejectsShapeDrift() {
        assertTrue(WorkspaceActionValidator.validate(
                WorkspaceActionId.ASSIGN_HOME,
                "minecraft:stone",
                "ITEM_ID",
                "",
                "building",
                null
        ).valid());

        WorkspaceActionValidation wrongCount = WorkspaceActionValidator.validate(
                WorkspaceActionId.PICKUP_TO_CURSOR,
                "minecraft:stone"
        );
        assertFalse(wrongCount.valid());
        assertTrue(wrongCount.diagnostics().startsWith("argument_count_mismatch"));

        WorkspaceActionValidation wrongType = WorkspaceActionValidator.validate(
                WorkspaceActionId.PICKUP_TO_CURSOR,
                "minecraft:stone",
                "ITEM_ID",
                "",
                "64"
        );
        assertFalse(wrongType.valid());
        assertTrue(wrongType.diagnostics().startsWith("argument_type_mismatch"));
    }

    @Test
    void packetRoundTripsTypedArgumentsThroughCatalogSchema() {
        WorkspaceActionEnvelope envelope = new WorkspaceActionEnvelope("session-a", 12, 99L);
        WorkspaceActionPacket packet = WorkspaceActionPacket.fromObjects(
                envelope,
                WorkspaceActionId.ASSIGN_HOME,
                "minecraft:stone",
                "ITEM_ID",
                "",
                "building",
                null
        );

        assertSame(envelope, packet.envelope());
        assertTrue(packet.validateShape().valid());
        Object[] arguments = packet.toObjects();
        assertEquals("minecraft:stone", arguments[0]);
        assertEquals("ITEM_ID", arguments[1]);
        assertEquals("", arguments[2]);
        assertEquals("building", arguments[3]);
        assertEquals(null, arguments[4]);
    }

    @Test
    void sessionEnvelopeRejectsStaleSessionAndWrongMenu() {
        WorkspaceActionSessionContext current = new WorkspaceActionSessionContext("session-a", 42, 10L);

        assertTrue(WorkspaceActionSessionValidator.validate(
                new WorkspaceActionEnvelope("session-a", 42, 8L),
                current
        ).valid());

        WorkspaceActionValidation stale = WorkspaceActionSessionValidator.validate(
                new WorkspaceActionEnvelope("session-b", 42, 8L),
                current
        );
        assertFalse(stale.valid());
        assertEquals("stale_session", stale.diagnostics());

        WorkspaceActionValidation wrongMenu = WorkspaceActionSessionValidator.validate(
                new WorkspaceActionEnvelope("session-a", 43, 8L),
                current
        );
        assertFalse(wrongMenu.valid());
        assertTrue(wrongMenu.diagnostics().startsWith("wrong_menu"));
    }

    @Test
    void packetCodecRoundTripsThroughCommonBufferContract() {
        WorkspaceActionPacket packet = WorkspaceActionPacket.fromObjects(
                new WorkspaceActionEnvelope("session-a", 7, 123L),
                WorkspaceActionId.MOVE_ISLAND,
                "island-tools",
                12.5D,
                -4.25D
        );
        InMemoryWorkspaceActionPacketBuffer buffer = new InMemoryWorkspaceActionPacketBuffer();

        WorkspaceActionPacketCodec.write(buffer, packet);
        WorkspaceActionPacket decoded = WorkspaceActionPacketCodec.read(buffer);

        assertEquals(packet.envelope(), decoded.envelope());
        assertEquals(packet.action(), decoded.action());
        assertEquals(java.util.List.of("island-tools", 12.5D, -4.25D), java.util.List.of(decoded.toObjects()));
    }
}
