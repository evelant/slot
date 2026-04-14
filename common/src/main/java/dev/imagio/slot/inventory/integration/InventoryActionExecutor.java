package dev.imagio.slot.inventory.integration;

import dev.imagio.slot.inventory.action.InventoryActionKind;
import dev.imagio.slot.inventory.action.InventoryActionMode;
import dev.imagio.slot.inventory.action.InventoryActionOutcome;
import dev.imagio.slot.inventory.action.InventoryActionRequest;
import dev.imagio.slot.inventory.action.InventoryActionStatus;
import dev.imagio.slot.inventory.action.InventoryCommandReasonCode;
import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.inventory.action.InventoryTargetCanonicalizer;
import dev.imagio.slot.inventory.core.InventoryActionPolicy;
import dev.imagio.slot.inventory.core.HostInstanceKey;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventoryPaneMembership;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.core.MenuCursorAccess;
import dev.imagio.slot.inventory.core.ToolRegionDescriptor;
import dev.imagio.slot.inventory.query.InventoryAuthorityReadService;
import dev.imagio.slot.workflow.domain.InventoryActivityConfidence;
import dev.imagio.slot.workflow.domain.InventoryActivityEvent;
import dev.imagio.slot.workflow.domain.InventoryActivityKind;
import dev.imagio.slot.workflow.domain.InventoryActivityProducer;
import dev.imagio.slot.workflow.domain.ProtectionPolicy;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class InventoryActionExecutor {
    private InventoryActionExecutor() {
    }

    public static InventoryActionOutcome execute(
            InventoryHostDescriptor host,
            ServerPlayer player,
            InventoryActionRequest request,
            ProtectionPolicy protectionPolicy
    ) {
        if (host == null || player == null || request == null) {
            return blocked(host, request, "missing_host_player_or_request", ItemStack.EMPTY);
        }
        if (request.serverMenuRef().containerId() >= 0 && request.serverMenuRef().containerId() != player.containerMenu.containerId) {
            return blocked(host, request, "menu_container_mismatch", ItemStack.EMPTY);
        }
        if (request.primaryTarget() != null && request.mode() == InventoryActionMode.SIMULATE && !simulationSupported(host, request.primaryTarget())) {
            return blocked(host, request, "primary_target_does_not_support_simulation", ItemStack.EMPTY);
        }
        if (request.secondaryTarget() != null && request.mode() == InventoryActionMode.SIMULATE && !simulationSupported(host, request.secondaryTarget())) {
            return blocked(host, request, "secondary_target_does_not_support_simulation", ItemStack.EMPTY);
        }
        if (request.primaryTarget() != null && !InventoryActionPolicy.allows(host, request.kind(), request.primaryTarget(), protectionPolicy)) {
            return blocked(host, request, "primary_target_blocked_by_policy", ItemStack.EMPTY);
        }
        if (request.secondaryTarget() != null && !InventoryActionPolicy.allows(host, request.kind(), request.secondaryTarget(), protectionPolicy)) {
            return blocked(host, request, "secondary_target_blocked_by_policy", ItemStack.EMPTY);
        }
        ProtectionState requestedState = requestedProtectionState(request);
        if (InventoryActionPolicy.blockedByProtection(request.kind(), requestedState.identity(), requestedState.stack(), protectionPolicy)) {
            return blocked(host, request, "requested_item_blocked_by_policy", requestedState.stack());
        }
        ProtectionState primaryState = resolveProtectionState(host, player, request.primaryTarget(), request);
        InventoryActionTarget primaryProtectionTarget = protectionTarget(host, request.primaryTarget());
        if (request.primaryTarget() != null
                && InventoryActionPolicy.blockedByProtection(
                request.kind(),
                primaryProtectionTarget,
                primaryState.identity(),
                primaryState.stack(),
                protectionPolicy
        )) {
            return blocked(host, request, "primary_item_blocked_by_policy", primaryState.stack());
        }
        ProtectionState secondaryState = resolveProtectionState(host, player, request.secondaryTarget(), request);
        InventoryActionTarget secondaryProtectionTarget = protectionTarget(host, request.secondaryTarget());
        if (request.secondaryTarget() != null
                && InventoryActionPolicy.blockedByProtection(
                request.kind(),
                secondaryProtectionTarget,
                secondaryState.identity(),
                secondaryState.stack(),
                protectionPolicy
        )) {
            return blocked(host, request, "secondary_item_blocked_by_policy", secondaryState.stack());
        }

        return switch (request.kind()) {
            case TRANSFER_ONE, TRANSFER_STACK, TRANSFER_ALL, EQUIP, UNEQUIP ->
                    executeTransfer(host, player, request);
            case PICKUP -> executePickup(host, player, request);
            case PLACE -> executePlace(host, player, request);
            case SWAP -> executeSwap(host, player, request);
            case QUICK_MOVE -> executeQuickMove(host, player, request);
            case DROP -> executeDrop(host, player, request);
            case USE -> executeUse(host, player, request);
            case TOOL_ACTIVATE -> fromTool(host, request, InventoryMutationRouter.activateTool(host, toolId(request), request.mode()));
            case TOOL_ACTION -> fromTool(host, request, InventoryMutationRouter.executeToolAction(
                    host,
                    toolId(request),
                    request.toolActionId(),
                    request.mode()
            ));
            case TOOL_TOGGLE -> fromTool(host, request, InventoryMutationRouter.setToolToggle(
                    host,
                    toolId(request),
                    request.toolToggleId(),
                    request.desiredToggleState(),
                    request.mode()
            ));
        };
    }

    private static InventoryActionOutcome executeTransfer(
            InventoryHostDescriptor host,
            ServerPlayer player,
            InventoryActionRequest request
    ) {
        BuiltinInventoryActionExecutor.ExecutionResult builtin = BuiltinInventoryActionExecutor.transfer(host, player, request);
        if (builtin.successful()) {
            return successful(
                    host,
                    request,
                    builtin.stackRemainder(),
                    explicitActivityEvents(host, request, builtin.actionStack(), builtin.stackRemainder())
            );
        }

        ProviderExtraction extraction = providerExtract(host, player, request);
        if (!extraction.successful()) {
            return blocked(host, request, builtin.diagnostics().isBlank() ? extraction.diagnostics() : builtin.diagnostics(), ItemStack.EMPTY);
        }

        ItemStack extracted = extraction.extracted();
        BuiltinInventoryActionExecutor.StackResult builtinInsert = BuiltinInventoryActionExecutor.insert(
                host,
                player,
                request.secondaryTarget(),
                request,
                extracted
        );
        if (builtinInsert.successful()) {
            if (request.mode() == dev.imagio.slot.inventory.action.InventoryActionMode.EXECUTE && !builtinInsert.stack().isEmpty()) {
                restoreProvider(host, player, request, builtinInsert.stack(), extraction);
            }
            return successful(
                    host,
                    request,
                    builtinInsert.stack(),
                    explicitActivityEvents(host, request, extracted, builtinInsert.stack())
            );
        }

        ProviderInsertion insertion = providerInsert(host, player, request, extracted);
        if (insertion.successful()) {
            if (request.mode() == dev.imagio.slot.inventory.action.InventoryActionMode.EXECUTE && !insertion.remainder().isEmpty()) {
                restoreProvider(host, player, request, insertion.remainder(), extraction);
            }
            return successful(
                    host,
                    request,
                    insertion.remainder(),
                    explicitActivityEvents(host, request, extracted, insertion.remainder())
            );
        }

        if (request.mode() == dev.imagio.slot.inventory.action.InventoryActionMode.EXECUTE) {
            restoreProvider(host, player, request, extracted, extraction);
        }
        return blocked(host, request, insertion.diagnostics().isBlank() ? builtinInsert.diagnostics() : insertion.diagnostics(), ItemStack.EMPTY);
    }

    private static InventoryActionOutcome executeDrop(
            InventoryHostDescriptor host,
            ServerPlayer player,
            InventoryActionRequest request
    ) {
        BuiltinInventoryActionExecutor.ExecutionResult builtin = BuiltinInventoryActionExecutor.drop(host, player, request);
        if (builtin.successful()) {
            return successful(host, request, ItemStack.EMPTY, List.of());
        }

        ProviderExtraction extraction = providerExtract(host, player, request);
        if (extraction.successful()) {
            if (request.mode() == InventoryActionMode.EXECUTE) {
                player.drop(extraction.extracted().copy(), false);
            }
            return successful(host, request, ItemStack.EMPTY, List.of());
        }
        return blocked(host, request, builtin.diagnostics().isBlank() ? extraction.diagnostics() : builtin.diagnostics(), ItemStack.EMPTY);
    }

    private static InventoryActionOutcome executeUse(
            InventoryHostDescriptor host,
            ServerPlayer player,
            InventoryActionRequest request
    ) {
        BuiltinInventoryActionExecutor.ExecutionResult builtin = BuiltinInventoryActionExecutor.use(host, player, request);
        if (builtin.successful()) {
            return successful(host, request, builtin.stackRemainder(), List.of());
        }
        return blocked(host, request, builtin.diagnostics(), ItemStack.EMPTY);
    }

    private static InventoryActionOutcome executePickup(
            InventoryHostDescriptor host,
            ServerPlayer player,
            InventoryActionRequest request
    ) {
        BuiltinInventoryActionExecutor.ExecutionResult builtin = BuiltinInventoryActionExecutor.pickup(host, player, request);
        if (builtin.successful()) {
            ItemStack pickedUp = builtin.stackRemainder();
            return successful(
                    host,
                    request,
                    pickedUp,
                    explicitActivityEvents(host, request, pickedUp, ItemStack.EMPTY)
            );
        }

        ProviderExtraction extraction = providerExtract(host, player, request);
        if (extraction.successful()) {
            return successful(
                    host,
                    request,
                    extraction.extracted(),
                    explicitActivityEvents(host, request, extraction.extracted(), ItemStack.EMPTY)
            );
        }
        return blocked(host, request, builtin.diagnostics().isBlank() ? extraction.diagnostics() : builtin.diagnostics(), ItemStack.EMPTY);
    }

    private static InventoryActionOutcome executePlace(
            InventoryHostDescriptor host,
            ServerPlayer player,
            InventoryActionRequest request
    ) {
        BuiltinInventoryActionExecutor.ExecutionResult builtin = BuiltinInventoryActionExecutor.place(host, player, request);
        if (builtin.successful()) {
            return successful(host, request, builtin.stackRemainder(), List.of());
        }

        ProviderInsertion insertion = providerInsert(host, player, request, MenuCursorAccess.get(host.menu()));
        if (insertion.successful()) {
            return successful(host, request, insertion.remainder(), List.of());
        }
        return blocked(host, request, builtin.diagnostics().isBlank() ? insertion.diagnostics() : builtin.diagnostics(), MenuCursorAccess.get(host.menu()));
    }

    private static InventoryActionOutcome executeSwap(
            InventoryHostDescriptor host,
            ServerPlayer player,
            InventoryActionRequest request
    ) {
        BuiltinInventoryActionExecutor.ExecutionResult builtin = BuiltinInventoryActionExecutor.swap(host, player, request);
        return builtin.successful()
                ? successful(host, request, builtin.stackRemainder(), List.of())
                : blocked(host, request, builtin.diagnostics(), MenuCursorAccess.get(host.menu()));
    }

    private static InventoryActionOutcome executeQuickMove(
            InventoryHostDescriptor host,
            ServerPlayer player,
            InventoryActionRequest request
    ) {
        if (request.secondaryTarget() != null) {
            return executeTransfer(host, player, request);
        }
        BuiltinInventoryActionExecutor.ExecutionResult builtin = BuiltinInventoryActionExecutor.quickMove(host, player, request);
        return builtin.successful()
                ? successful(host, request, builtin.stackRemainder(), List.of())
                : blocked(host, request, builtin.diagnostics(), ItemStack.EMPTY);
    }

    private static ProviderExtraction providerExtract(
            InventoryHostDescriptor host,
            ServerPlayer player,
            InventoryActionRequest request
    ) {
        InventoryActionTarget sourceTarget = request.primaryTarget();
        String sourceId = InventoryAuthorityReadService.sourceId(host, sourceTarget);
        if (sourceId == null || sourceId.isBlank()) {
            return ProviderExtraction.blocked("provider_source_unavailable");
        }

        InventorySourceDescriptor source = host.source(sourceId);
        if (source == null || !source.providerBacked()) {
            return ProviderExtraction.blocked("source_is_not_provider_backed");
        }

        MutationResult result = InventoryMutationRouter.mutate(
                host,
                extractionRequest(host, player, sourceTarget, request.identity(), transferMode(request.kind())),
                InventoryMutationMode.valueOf(request.mode().name())
        );
        if (!result.successful() || result.stackRemainder().isEmpty()) {
            return ProviderExtraction.blocked(result.diagnostics());
        }
        return ProviderExtraction.success(result.stackRemainder(), request.identity());
    }

    private static ProviderInsertion providerInsert(
            InventoryHostDescriptor host,
            ServerPlayer player,
            InventoryActionRequest request,
            ItemStack stack
    ) {
        InventoryActionTarget target = request.secondaryTarget() != null ? request.secondaryTarget() : request.primaryTarget();
        String sourceId = InventoryAuthorityReadService.sourceId(host, target);
        if (sourceId == null || sourceId.isBlank()) {
            return ProviderInsertion.blocked("provider_target_unavailable");
        }

        InventorySourceDescriptor source = host.source(sourceId);
        if (source == null || !source.providerBacked()) {
            return ProviderInsertion.blocked("target_is_not_provider_backed");
        }

        MutationResult result = InventoryMutationRouter.mutate(
                host,
                insertionRequest(host, player, target, stack),
                InventoryMutationMode.valueOf(request.mode().name())
        );
        return result.successful()
                ? ProviderInsertion.success(result.stackRemainder())
                : ProviderInsertion.blocked(result.diagnostics());
    }

    private static void restoreProvider(
            InventoryHostDescriptor host,
            ServerPlayer player,
            InventoryActionRequest request,
            ItemStack remainder,
            ProviderExtraction extraction
    ) {
        if (remainder == null || remainder.isEmpty()) {
            return;
        }
        String sourceId = InventoryAuthorityReadService.sourceId(host, request.primaryTarget());
        if (sourceId == null || sourceId.isBlank()) {
            return;
        }
        InventoryMutationRouter.mutate(
                host,
                restoreRequest(host, player, request.primaryTarget(), remainder),
                InventoryMutationMode.EXECUTE
        );
    }

    private static InventoryMutationRequest extractionRequest(
            InventoryHostDescriptor host,
            ServerPlayer player,
            InventoryActionTarget sourceTarget,
            dev.imagio.slot.inventory.core.ItemIdentity identity,
            InventoryTransferMode transferMode
    ) {
        String sourceId = InventoryAuthorityReadService.sourceId(host, sourceTarget);
        String entryId = InventoryAuthorityReadService.entryId(sourceTarget);
        if (!entryId.isBlank()) {
            return InventoryMutationRequest.extract(host, player, sourceId, entryId, identity, transferMode);
        }
        int slotIndex = InventoryAuthorityReadService.slotIndex(host, sourceTarget);
        return slotIndex >= 0
                ? InventoryMutationRequest.extract(host, player, sourceId, slotIndex, identity, transferMode)
                : InventoryMutationRequest.extract(host, player, sourceId, identity, transferMode);
    }

    private static InventoryMutationRequest insertionRequest(
            InventoryHostDescriptor host,
            ServerPlayer player,
            InventoryActionTarget target,
            ItemStack stack
    ) {
        String sourceId = InventoryAuthorityReadService.sourceId(host, target);
        String entryId = InventoryAuthorityReadService.entryId(target);
        if (!entryId.isBlank()) {
            return InventoryMutationRequest.insert(host, player, sourceId, entryId, stack);
        }
        int slotIndex = InventoryAuthorityReadService.slotIndex(host, target);
        return slotIndex >= 0
                ? InventoryMutationRequest.insert(host, player, sourceId, slotIndex, stack)
                : InventoryMutationRequest.insert(host, player, sourceId, stack);
    }

    private static InventoryMutationRequest restoreRequest(
            InventoryHostDescriptor host,
            ServerPlayer player,
            InventoryActionTarget sourceTarget,
            ItemStack stack
    ) {
        String sourceId = InventoryAuthorityReadService.sourceId(host, sourceTarget);
        String entryId = InventoryAuthorityReadService.entryId(sourceTarget);
        if (!entryId.isBlank()) {
            return InventoryMutationRequest.insert(host, player, sourceId, stack);
        }
        int slotIndex = InventoryAuthorityReadService.slotIndex(host, sourceTarget);
        return slotIndex >= 0
                ? InventoryMutationRequest.insert(host, player, sourceId, slotIndex, stack)
                : InventoryMutationRequest.insert(host, player, sourceId, stack);
    }

    private static InventoryTransferMode transferMode(InventoryActionKind kind) {
        return switch (kind) {
            case TRANSFER_ONE -> InventoryTransferMode.ONE;
            case TRANSFER_STACK, EQUIP, UNEQUIP, DROP -> InventoryTransferMode.STACK;
            case TRANSFER_ALL -> InventoryTransferMode.ALL;
            default -> InventoryTransferMode.STACK;
        };
    }

    private static String toolId(InventoryActionRequest request) {
        if (request == null || request.primaryTarget() == null) {
            return "";
        }
        return switch (request.primaryTarget()) {
            case InventoryActionTarget.CursorTarget ignored -> "";
            case InventoryActionTarget.ToolRegionTarget toolRegionTarget -> toolRegionTarget.toolId();
            case InventoryActionTarget.ToolControlTarget toolControlTarget -> toolControlTarget.toolId();
            default -> "";
        };
    }

    private static List<InventoryActivityEvent> explicitActivityEvents(
            InventoryHostDescriptor host,
            InventoryActionRequest request,
            ItemStack movedStack,
            ItemStack remainder
    ) {
        if (!isExplicitAcquisition(host, request) || movedStack == null || movedStack.isEmpty()) {
            return List.of();
        }
        dev.imagio.slot.inventory.core.ItemIdentity identity = request != null && request.identity() != null
                ? request.identity()
                : ItemIdentityMatcher.create(movedStack);
        int count = Math.max(0, movedStack.getCount() - (remainder == null ? 0 : remainder.getCount()));
        if (identity == null || count <= 0) {
            return List.of();
        }
        return List.of(new InventoryActivityEvent(
                explicitActivityKind(host, request),
                explicitProducerId(host, request),
                InventoryActivityConfidence.AUTHORITATIVE,
                identity,
                count,
                request == null ? null : request.primaryTarget(),
                request == null ? null : request.secondaryTarget(),
                request == null ? "" : request.requestId(),
                "",
                List.of(),
                ""
        ));
    }

    private static boolean isExplicitAcquisition(
            InventoryHostDescriptor host,
            InventoryActionRequest request
    ) {
        if (host == null || request == null || !request.mode().equals(InventoryActionMode.EXECUTE)) {
            return false;
        }
        return isExternalWithdrawalIntoCarried(host, request) || isToolOutputExtraction(host, request);
    }

    private static InventoryActivityKind explicitActivityKind(
            InventoryHostDescriptor host,
            InventoryActionRequest request
    ) {
        return isToolOutputExtraction(host, request) ? InventoryActivityKind.CRAFTED : InventoryActivityKind.ACQUIRED;
    }

    private static InventoryActivityProducer explicitProducerId(
            InventoryHostDescriptor host,
            InventoryActionRequest request
    ) {
        return isToolOutputExtraction(host, request)
                ? InventoryActivityProducer.TOOL_OUTPUT_EXTRACTION
                : InventoryActivityProducer.EXTERNAL_WITHDRAWAL;
    }

    private static boolean isExternalWithdrawalIntoCarried(
            InventoryHostDescriptor host,
            InventoryActionRequest request
    ) {
        InventorySourceDescriptor source = sourceDescriptor(host, request.primaryTarget());
        if (source == null || source.paneMembership() != InventoryPaneMembership.EXTERNAL) {
            return false;
        }
        if (request.kind() == InventoryActionKind.PICKUP) {
            return true;
        }
        InventoryActionTarget destination = request.secondaryTarget();
        return destination != null && targetIsCarried(host, destination);
    }

    private static boolean isToolOutputExtraction(
            InventoryHostDescriptor host,
            InventoryActionRequest request
    ) {
        if (host == null || request == null) {
            return false;
        }
        if (!(request.primaryTarget() instanceof InventoryActionTarget.ToolRegionTarget regionTarget)) {
            return false;
        }
        ToolRegionDescriptor region = toolRegion(host, regionTarget);
        return region != null && region.role() == dev.imagio.slot.inventory.core.ToolRegionRole.OUTPUT;
    }

    private static boolean targetIsCarried(InventoryHostDescriptor host, InventoryActionTarget target) {
        if (host == null || target == null) {
            return false;
        }
        return switch (target) {
            case InventoryActionTarget.CursorTarget ignored -> false;
            case InventoryActionTarget.SourceTarget sourceTarget -> {
                InventorySourceDescriptor source = host.source(sourceTarget.sourceId());
                yield source != null && source.paneMembership() == InventoryPaneMembership.CARRIED;
            }
            case InventoryActionTarget.SourceSlotTarget slotTarget -> {
                InventorySourceDescriptor source = host.source(slotTarget.sourceId());
                yield source != null && source.paneMembership() == InventoryPaneMembership.CARRIED;
            }
            case InventoryActionTarget.SourceEntryTarget sourceEntryTarget -> {
                InventorySourceDescriptor source = host.source(sourceEntryTarget.sourceId());
                yield source != null && source.paneMembership() == InventoryPaneMembership.CARRIED;
            }
            case InventoryActionTarget.QuickAccessTarget ignored -> true;
            case InventoryActionTarget.EquipmentTarget ignored -> true;
            case InventoryActionTarget.ToolRegionTarget toolRegionTarget -> {
                String sourceId = InventoryAuthorityReadService.sourceId(host, toolRegionTarget);
                InventorySourceDescriptor source = sourceId.isBlank() ? null : host.source(sourceId);
                yield source != null && source.paneMembership() == InventoryPaneMembership.CARRIED;
            }
            case InventoryActionTarget.ToolControlTarget ignored -> false;
        };
    }

    private static InventorySourceDescriptor sourceDescriptor(
            InventoryHostDescriptor host,
            InventoryActionTarget target
    ) {
        String sourceId = InventoryAuthorityReadService.sourceId(host, target);
        return sourceId == null || sourceId.isBlank() ? null : host.source(sourceId);
    }

    private static ToolRegionDescriptor toolRegion(
            InventoryHostDescriptor host,
            InventoryActionTarget.ToolRegionTarget target
    ) {
        if (host == null || target == null) {
            return null;
        }
        dev.imagio.slot.inventory.core.InventoryToolDescriptor tool = host.tool(target.toolId());
        if (tool == null) {
            return null;
        }
        return tool.regions().stream()
                .filter(region -> target.regionId().equals(region.id()))
                .findFirst()
                .orElse(null);
    }

    private static InventoryActionTarget protectionTarget(
            InventoryHostDescriptor host,
            InventoryActionTarget target
    ) {
        if (host == null || target == null) {
            return target;
        }
        return InventoryTargetCanonicalizer.canonicalTarget(host, target);
    }

    private static boolean simulationSupported(
            InventoryHostDescriptor host,
            InventoryActionTarget target
    ) {
        if (host == null || target == null) {
            return false;
        }
        if (target instanceof InventoryActionTarget.ToolRegionTarget regionTarget) {
            ToolRegionDescriptor region = toolRegion(host, regionTarget);
            if (region == null) {
                return false;
            }
            if (!region.linkedSourceId().isBlank()) {
                InventorySourceDescriptor source = host.source(region.linkedSourceId());
                return source == null || source.simulationSupported();
            }
            return region.simulationSupported();
        }
        InventorySourceDescriptor source = sourceDescriptor(host, target);
        return source == null || source.simulationSupported();
    }

    private static ProtectionState requestedProtectionState(InventoryActionRequest request) {
        if (request == null) {
            return ProtectionState.empty();
        }
        ItemStack stack = request.stack() == null ? ItemStack.EMPTY : request.stack().copy();
        if (!stack.isEmpty()) {
            return new ProtectionState(
                    request.identity() == null ? ItemIdentityMatcher.create(stack) : request.identity(),
                    stack
            );
        }
        return new ProtectionState(request.identity(), ItemStack.EMPTY);
    }

    private static ProtectionState resolveProtectionState(
            InventoryHostDescriptor host,
            ServerPlayer player,
            InventoryActionTarget target,
            InventoryActionRequest request
    ) {
        if (host == null || player == null || target == null) {
            return ProtectionState.empty();
        }

        ItemStack stack = InventoryAuthorityReadService.currentStack(host, player, target);
        if (!stack.isEmpty()) {
            return new ProtectionState(ItemIdentityMatcher.create(stack), stack);
        }

        if (request != null
                && request.primaryTarget() != null
                && InventoryTargetCanonicalizer.canonicalKey(host, request.primaryTarget())
                .equals(InventoryTargetCanonicalizer.canonicalKey(host, target))) {
            return requestedProtectionState(request);
        }
        return ProtectionState.empty();
    }

    private static int slotIndex(
            InventoryHostDescriptor host,
            InventoryActionTarget target
    ) {
        return InventoryAuthorityReadService.slotIndex(host, target);
    }

    private static InventoryActionOutcome fromTool(
            InventoryHostDescriptor host,
            InventoryActionRequest request,
            ToolActionResult result
    ) {
        return result != null && result.successful()
                ? successful(host, request, ItemStack.EMPTY, List.of())
                : blocked(host, request, result == null ? "tool_action_failed" : result.diagnostics(), ItemStack.EMPTY);
    }

    private static InventoryActionOutcome successful(
            InventoryHostDescriptor host,
            InventoryActionRequest request,
            ItemStack remainder,
            List<InventoryActivityEvent> activityEvents
    ) {
        int requestedCount = request == null ? 0 : request.requestedCount();
        int appliedCount = requestedCount > 0
                ? requestedCount
                : (request == null || request.stack() == null ? 0 : request.stack().getCount());
        return new InventoryActionOutcome(
                resolvedHostKey(host, request),
                host.serverMenuRef(),
                request.requestId(),
                request.kind(),
                request.mode(),
                request.origin(),
                request.correlationId(),
                request.causationId(),
                request.sessionId(),
                request.primaryTarget(),
                request.secondaryTarget(),
                InventoryActionStatus.SUCCESS,
                List.of(),
                requestedCount,
                appliedCount,
                false,
                activityEvents,
                remainder,
                ""
        );
    }

    private static InventoryActionOutcome blocked(
            InventoryHostDescriptor host,
            InventoryActionRequest request,
            String diagnostics,
            ItemStack remainder
    ) {
        String resolvedDiagnostics = diagnostics == null ? "" : diagnostics;
        return new InventoryActionOutcome(
                resolvedHostKey(host, request),
                host == null ? (request == null ? null : request.serverMenuRef()) : host.serverMenuRef(),
                request == null ? "" : request.requestId(),
                request == null ? InventoryActionKind.TRANSFER_ONE : request.kind(),
                request == null ? dev.imagio.slot.inventory.action.InventoryActionMode.EXECUTE : request.mode(),
                request == null ? "" : request.origin(),
                request == null ? "" : request.correlationId(),
                request == null ? "" : request.causationId(),
                request == null ? "" : request.sessionId(),
                request == null ? null : request.primaryTarget(),
                request == null ? null : request.secondaryTarget(),
                InventoryActionStatus.BLOCKED,
                InventoryCommandReasonCode.fromDiagnostics(resolvedDiagnostics.isBlank() ? List.of() : List.of(resolvedDiagnostics)),
                request == null ? 0 : request.requestedCount(),
                0,
                false,
                List.of(),
                remainder,
                resolvedDiagnostics
        );
    }

    private record ProviderExtraction(
            boolean successful,
            ItemStack extracted,
            dev.imagio.slot.inventory.core.ItemIdentity identity,
            String diagnostics
    ) {
        private static ProviderExtraction success(ItemStack extracted, dev.imagio.slot.inventory.core.ItemIdentity identity) {
            return new ProviderExtraction(true, extracted == null ? ItemStack.EMPTY : extracted, identity, "");
        }

        private static ProviderExtraction blocked(String diagnostics) {
            return new ProviderExtraction(false, ItemStack.EMPTY, null, diagnostics == null ? "" : diagnostics);
        }
    }

    private record ProviderInsertion(
            boolean successful,
            ItemStack remainder,
            String diagnostics
    ) {
        private static ProviderInsertion success(ItemStack remainder) {
            return new ProviderInsertion(true, remainder == null ? ItemStack.EMPTY : remainder, "");
        }

        private static ProviderInsertion blocked(String diagnostics) {
            return new ProviderInsertion(false, ItemStack.EMPTY, diagnostics == null ? "" : diagnostics);
        }
    }

    private record ProtectionState(
            dev.imagio.slot.inventory.core.ItemIdentity identity,
            ItemStack stack
    ) {
        private ProtectionState {
            stack = stack == null ? ItemStack.EMPTY : stack;
        }

        private static ProtectionState empty() {
            return new ProtectionState(null, ItemStack.EMPTY);
        }
    }

    private static HostInstanceKey resolvedHostKey(
            InventoryHostDescriptor host,
            InventoryActionRequest request
    ) {
        if (request != null && request.hostId() != null && !request.hostId().equals(HostInstanceKey.empty())) {
            return request.hostId();
        }
        return host == null ? HostInstanceKey.empty() : host.hostId();
    }
}
