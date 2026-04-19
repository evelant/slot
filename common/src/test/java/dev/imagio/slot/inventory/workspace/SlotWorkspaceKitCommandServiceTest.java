package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.action.InventoryActionKind;
import dev.imagio.slot.inventory.action.InventoryActionMode;
import dev.imagio.slot.inventory.action.InventoryActionOutcome;
import dev.imagio.slot.inventory.action.InventoryActionQuantity;
import dev.imagio.slot.inventory.action.InventoryActionRequest;
import dev.imagio.slot.inventory.action.InventoryActionScope;
import dev.imagio.slot.inventory.action.InventoryActionStatus;
import dev.imagio.slot.inventory.action.InventoryCommandReasonCode;
import dev.imagio.slot.inventory.core.HostInstanceKey;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ServerMenuRef;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.workflow.domain.InMemoryWorkflowDomainStateRepository;
import dev.imagio.slot.workflow.domain.KitDefinition;
import dev.imagio.slot.workflow.domain.ProtectionPolicy;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlotWorkspaceKitCommandServiceTest {

    @Test
    void saveBeltAsKitRecordsKit() {
        WorkflowDomainRuntime runtime = runtime();

        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.saveBeltAsKit(
                runtime,
                InventoryAuthoritySnapshot.empty(),
                IDENTITY_RESOLVER,
                "Mining"
        );

        assertTrue(outcome.success());
        assertEquals("kit saved", outcome.status());
        List<KitDefinition> kits = runtime.kitWorkflow().kits();
        assertEquals(1, kits.size());
        assertEquals("Mining", kits.get(0).name());
    }

    @Test
    void saveBeltAsKitFallsBackToGeneratedName() {
        WorkflowDomainRuntime runtime = runtime();

        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.saveBeltAsKit(
                runtime,
                InventoryAuthoritySnapshot.empty(),
                IDENTITY_RESOLVER,
                ""
        );

        assertTrue(outcome.success());
        assertEquals("Kit 1", runtime.kitWorkflow().kits().get(0).name());
    }

    @Test
    void activateKitFlipsActivationAndReturnsAcceptedOutcome() {
        WorkflowDomainRuntime runtime = runtime();
        KitDefinition kit = runtime.kitWorkflow().create("Mining");
        RecordingActionExecutor executor = new RecordingActionExecutor();

        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.activateKit(
                runtime,
                InventoryAuthoritySnapshot.empty(),
                ProtectionPolicy.allowAll(),
                IDENTITY_RESOLVER,
                executor,
                kit.id()
        );

        assertTrue(outcome.success());
        assertEquals(kit.id(), runtime.kitWorkflow().activation().kitId());
    }

    @Test
    void activateKitRejectsUnknownId() {
        WorkflowDomainRuntime runtime = runtime();

        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.activateKit(
                runtime,
                InventoryAuthoritySnapshot.empty(),
                ProtectionPolicy.allowAll(),
                IDENTITY_RESOLVER,
                new RecordingActionExecutor(),
                "missing"
        );

        assertFalse(outcome.success());
        assertEquals("unknown_kit", outcome.diagnostics());
    }

    @Test
    void deactivateKitClearsActivation() {
        WorkflowDomainRuntime runtime = runtime();
        KitDefinition kit = runtime.kitWorkflow().create("Mining");
        runtime.kitWorkflow().activate(kit.id());

        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.deactivateKit(runtime);

        assertTrue(outcome.success());
        assertFalse(runtime.kitWorkflow().activation().isActive());
    }

    @Test
    void deactivateKitRejectsWhenNoneActive() {
        WorkflowDomainRuntime runtime = runtime();

        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.deactivateKit(runtime);

        assertFalse(outcome.success());
        assertEquals("no_active_kit", outcome.diagnostics());
    }

    @Test
    void deleteKitRemovesKit() {
        WorkflowDomainRuntime runtime = runtime();
        KitDefinition kit = runtime.kitWorkflow().create("Mining");

        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.deleteKit(runtime, kit.id());

        assertTrue(outcome.success());
        assertNull(runtime.kitWorkflow().kit(kit.id()));
    }

    @Test
    void deleteKitRejectsUnknownId() {
        WorkflowDomainRuntime runtime = runtime();

        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.deleteKit(runtime, "missing");

        assertFalse(outcome.success());
        assertEquals("unknown_kit", outcome.diagnostics());
    }

    private static WorkflowDomainRuntime runtime() {
        return new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
    }

    private static final Function<InventoryEntrySnapshot, ItemIdentity> IDENTITY_RESOLVER =
            entry -> entry == null ? null : ItemIdentity.of(entry.stack().itemId());

    private static final class RecordingActionExecutor implements Function<InventoryActionRequest, InventoryActionOutcome> {
        final List<InventoryActionRequest> requests = new ArrayList<>();

        @Override
        public InventoryActionOutcome apply(InventoryActionRequest request) {
            requests.add(request);
            assertNotNull(request);
            return new InventoryActionOutcome(
                    HostInstanceKey.empty(),
                    new ServerMenuRef("", -1),
                    request.requestId(),
                    request.kind() == null ? InventoryActionKind.TRANSFER : request.kind(),
                    InventoryActionMode.EXECUTE,
                    InventoryActionQuantity.DEFAULT,
                    InventoryActionScope.BEST_SINGLE_SOURCE,
                    request.conflictPolicy(),
                    request.origin(),
                    request.primaryTarget(),
                    request.secondaryTarget(),
                    InventoryActionStatus.SUCCESS,
                    List.<InventoryCommandReasonCode>of(),
                    1,
                    1,
                    false,
                    List.of(),
                    ItemStack.EMPTY,
                    ""
            );
        }
    }
}
