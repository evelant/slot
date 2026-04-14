package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.client.policy.ItemBehaviorPolicy;
import dev.imagio.slot.client.session.SlotScreenSessionResolver;
import dev.imagio.slot.session.ChestLikeMenuLayout;
import dev.imagio.slot.intent.ActionFamily;
import dev.imagio.slot.intent.ActionRequest;
import dev.imagio.slot.operation.ActionOutcome;
import dev.imagio.slot.operation.ActionReason;
import dev.imagio.slot.operation.RefreshScope;
import dev.imagio.slot.session.InventoryHostDescriptor;
import dev.imagio.slot.session.SlotSessionDescriptor;
import dev.imagio.slot.storage.adapter.ExternalToolSlotRole;
import dev.imagio.slot.storage.adapter.ExternalToolSpec;
import dev.imagio.slot.session.StorageViewResolver;
import dev.imagio.slot.storage.provider.StorageViewProviderSession;
import dev.imagio.slot.network.BackpackTransferActionRequests;
import dev.imagio.slot.network.BackpackTransferPayload;
import dev.imagio.slot.network.CraftingGridActionRequests;
import dev.imagio.slot.network.CraftingGridPlacementPayload;
import dev.imagio.slot.network.CursorTransferActionRequests;
import dev.imagio.slot.network.CursorTransferPayload;
import dev.imagio.slot.network.ToolActionRequests;
import dev.imagio.slot.recent.AcquisitionProducerId;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

final class SlotServerActionDispatcher {
    private SlotServerActionDispatcher() {
    }

    static SlotServerDispatchResult dispatch(ServerPlayer player, ActionRequest request) {
        if (player == null || request == null) {
            return SlotServerDispatchResult.of(
                    ActionOutcome.failed(null, ActionFamily.TRANSFER, ActionReason.INTERNAL_ERROR, RefreshScope.NONE),
                    false,
                    Map.of()
            );
        }

        AbstractContainerMenu menu = player.containerMenu;
        if (menu == null || request.expectedContainerId() != menu.containerId) {
            return blocked(request, ActionReason.STALE_SESSION);
        }

        SlotSessionDescriptor descriptor = SlotScreenSessionResolver.resolveMenuDescriptor(null, menu, player.getInventory(), null);
        if (!request.expectedSessionFingerprint().isBlank()
                && !request.expectedSessionFingerprint().equals(descriptor.fingerprint())) {
            SlotDebugLog.log(
                    "Rejected action request due to fingerprint mismatch: requestId={} expected={} actual={} actionFamily={}",
                    request.requestId(),
                    request.expectedSessionFingerprint(),
                    descriptor.fingerprint(),
                    request.actionFamily()
            );
            return blocked(request, ActionReason.STALE_SESSION);
        }

        return switch (request.actionFamily()) {
            case TRANSFER, STORE -> dispatchBackpackTransfer(player, menu, request);
            case PICKUP, DROP, TRASH, VOID -> dispatchCursorTransfer(player, menu, request);
            case CRAFT -> dispatchCrafting(player, menu, request);
            case TOOL_ACTION -> dispatchToolAction(player, menu, request);
            default -> blocked(request, ActionReason.UNSUPPORTED_SOURCE);
        };
    }

    private static SlotServerDispatchResult dispatchBackpackTransfer(
            ServerPlayer player,
            AbstractContainerMenu menu,
            ActionRequest request
    ) {
        BackpackTransferActionRequests.LegacyResolution resolution = BackpackTransferActionRequests.resolve(request);
        if (resolution == null) {
            return blocked(request, ActionReason.UNSUPPORTED_SOURCE);
        }

        Map<UUID, CompoundTag> syncedContents = new LinkedHashMap<>();
        BackpackTransferPayload payload = resolution.payload();
        int movedCount;
        switch (resolution.route()) {
            case BACKPACK_TO_MENU -> {
                movedCount = BackpackTransferOperations.handleBackpackToMenu(player, menu, resolution.spec(), syncedContents);
                return movedCount > 0
                        ? confirmed(request, movedCount, syncedContents)
                        : blocked(request, ActionReason.NO_MATCHING_SOURCE);
            }
            case EXTERNAL_TO_CARRIED, MENU_TO_EXTERNAL, CARRIED_TO_EXTERNAL, BACKPACK_TO_EXTERNAL -> {
                InventoryHostDescriptor host = StorageViewResolver.resolve(null, menu, player.getInventory(), null);
                ChestLikeMenuLayout layout = host == null ? null : host.layout();
                if (layout == null || layout.primaryStorageIsCarried()) {
                    return blocked(request, ActionReason.UNSUPPORTED_SOURCE);
                }

                StorageViewProviderSession primaryStorageSession = layout.primaryStorageSession();
                movedCount = switch (resolution.route()) {
                    case EXTERNAL_TO_CARRIED -> BackpackTransferOperations.handleExternalToCarried(
                            player, menu, layout, primaryStorageSession, payload, syncedContents
                    );
                    case MENU_TO_EXTERNAL -> BackpackTransferOperations.handleMenuToExternal(
                            player, menu, layout, primaryStorageSession, payload, syncedContents
                    );
                    case CARRIED_TO_EXTERNAL -> BackpackTransferOperations.handleCarriedToExternal(
                            player, menu, layout, primaryStorageSession, payload, syncedContents
                    );
                    case BACKPACK_TO_EXTERNAL -> BackpackTransferOperations.handleBackpackToExternal(
                            player, menu, primaryStorageSession, payload, syncedContents
                    );
                    case BACKPACK_TO_MENU -> 0;
                };

                if (movedCount <= 0) {
                    return blocked(request, ActionReason.NO_MATCHING_SOURCE);
                }
                RecentAcquisitionAttribution acquisition = SlotRecentAcquisitionSupport.forBackpackTransfer(
                        resolution.route(),
                        payload.identity() == null ? "" : payload.identity().itemId()
                );
                return acquisition.present()
                        ? confirmed(
                                request,
                                movedCount,
                                syncedContents,
                                "",
                                acquisition.producerId(),
                                acquisition.itemIds()
                        )
                        : confirmed(request, movedCount, syncedContents);
            }
        }
        return blocked(request, ActionReason.UNSUPPORTED_SOURCE);
    }

    private static SlotServerDispatchResult dispatchCursorTransfer(
            ServerPlayer player,
            AbstractContainerMenu menu,
            ActionRequest request
    ) {
        CursorTransferActionRequests.LegacyResolution resolution = CursorTransferActionRequests.resolve(request);
        if (resolution == null) {
            return blocked(request, ActionReason.UNSUPPORTED_SOURCE);
        }

        InventoryHostDescriptor host = menu instanceof InventoryMenu
                ? null
                : StorageViewResolver.resolve(null, menu, player.getInventory(), null);
        ChestLikeMenuLayout layout = host == null ? null : host.layout();

        Map<UUID, CompoundTag> syncedContents = new LinkedHashMap<>();
        CursorTransferPayload payload = resolution.payload();
        int movedCount = switch (resolution.route()) {
            case PICKUP_MATCHING -> CursorTransferOperations.handlePickupMatching(player, menu, layout, payload, syncedContents);
            case DROP_CARRIED -> CursorTransferOperations.handleDropCarried(player, menu, layout, payload, syncedContents);
            case DROP_CARRIED_TO_SLOT -> CursorTransferOperations.handleDropCarriedToSlot(menu, payload);
            case TRASH_CARRIED -> CursorTransferOperations.handleTrashCarried(menu, payload);
            case VOID_MATCHING_CARRIED -> CursorTransferOperations.handleVoidMatchingCarried(player, menu, layout, payload, syncedContents);
        };

        if (movedCount <= 0) {
            return blocked(request, cursorFailureReason(menu, payload, resolution.route()));
        }
        RecentAcquisitionAttribution acquisition = SlotRecentAcquisitionSupport.forCursorTransfer(
                resolution.route(),
                payload.targetPane() == CursorTransferPayload.TargetPane.OPEN_CONTAINER,
                payload.identity() == null ? "" : payload.identity().itemId()
        );
        return acquisition.present()
                ? confirmed(
                        request,
                        movedCount,
                        syncedContents,
                        "",
                        acquisition.producerId(),
                        acquisition.itemIds()
                )
                : confirmed(request, movedCount, syncedContents);
    }

    private static SlotServerDispatchResult dispatchCrafting(
            ServerPlayer player,
            AbstractContainerMenu menu,
            ActionRequest request
    ) {
        CraftingGridActionRequests.Resolution resolution = CraftingGridActionRequests.resolve(request);
        if (resolution == null) {
            return blocked(request, ActionReason.UNSUPPORTED_SOURCE);
        }

        CraftingGridActionRequests.CraftSpec spec = resolution.spec();
        if (requiresCraftIdentity(resolution.route()) && spec.identity() == null) {
            return blocked(request, ActionReason.IDENTITY_MISSING);
        }

        List<ExternalToolSpec> craftingTools = CraftingToolSupport.resolveCraftingTools(menu, player);
        if (craftingTools.isEmpty()) {
            return blocked(request, ActionReason.UNSUPPORTED_SOURCE);
        }

        List<Integer> targetMenuSlots = spec.targetMenuSlots();
        if (targetMenuSlots.isEmpty()) {
            return blocked(request, ActionReason.TARGET_MISSING);
        }

        ExternalToolSlotRole requiredTargetRole = requiredCraftTargetRole(resolution.route());
        Set<Integer> validTargets = craftingTools.stream()
                .flatMap(tool -> tool.menuSlotsForRole(requiredTargetRole).stream())
                .collect(java.util.stream.Collectors.toSet());
        if (!validTargets.containsAll(targetMenuSlots)) {
            SlotDebugLog.log(
                    "Craft grid place rejected target slots for role {}: menu={} targetMenuSlots={} craftingTools={}",
                    requiredTargetRole,
                    menu.getClass().getName(),
                    targetMenuSlots,
                    craftingTools.stream().map(ExternalToolSpec::id).toList()
            );
            return blocked(request, ActionReason.TARGET_MISSING, summaryKeyForCraftRoute(resolution.route()));
        }

        return switch (resolution.route()) {
            case PANE_IDENTITY_PLACE -> dispatchPaneCraftPlacement(request, player, menu, spec);
            case CURSOR_PLACE -> dispatchCursorCraftPlacement(request, menu, spec);
            case CURSOR_DISTRIBUTE -> dispatchCursorCraftDistribution(request, menu, spec);
            case RESULT_EXTRACT -> dispatchCraftResultExtraction(request, player, menu, spec, craftingTools);
        };
    }

    private static SlotServerDispatchResult dispatchToolAction(
            ServerPlayer player,
            AbstractContainerMenu menu,
            ActionRequest request
    ) {
        ToolActionRequests.Resolution resolution = ToolActionRequests.resolve(request);
        if (resolution == null) {
            return blocked(request, ActionReason.UNSUPPORTED_TOOL);
        }

        ToolActionSupport.ToolActionResult result = ToolActionSupport.apply(menu, player, resolution.toolId(), resolution.action());
        return result.applied()
                ? confirmed(request, 1, Map.of())
                : result.failed()
                ? failed(request, result.reason(), result.summaryKey())
                : blocked(request, result.reason(), result.summaryKey());
    }

    private static SlotServerDispatchResult confirmed(
            ActionRequest request,
            int affectedCount,
            Map<UUID, CompoundTag> syncedContents
    ) {
        return confirmed(request, affectedCount, syncedContents, "");
    }

    private static SlotServerDispatchResult confirmed(
            ActionRequest request,
            int affectedCount,
            Map<UUID, CompoundTag> syncedContents,
            String summaryKey
    ) {
        return confirmed(request, affectedCount, syncedContents, summaryKey, AcquisitionProducerId.UNKNOWN, List.of());
    }

    private static SlotServerDispatchResult confirmed(
            ActionRequest request,
            int affectedCount,
            Map<UUID, CompoundTag> syncedContents,
            String summaryKey,
            AcquisitionProducerId acquisitionProducerId,
            List<String> acquisitionItemIds
    ) {
        return SlotServerDispatchResult.of(
                new ActionOutcome(
                        request.requestId(),
                        request.actionFamily(),
                        dev.imagio.slot.operation.ActionStatus.CONFIRMED,
                        ActionReason.NONE,
                        affectedCount,
                        RefreshScope.SESSION,
                        List.of(),
                        acquisitionItemIds,
                        acquisitionProducerId == null ? "" : acquisitionProducerId.serializedId(),
                        summaryKey == null ? "" : summaryKey
                ),
                true,
                syncedContents
        );
    }

    private static ActionReason cursorFailureReason(
            AbstractContainerMenu menu,
            CursorTransferPayload payload,
            CursorTransferActionRequests.Route route
    ) {
        return switch (route) {
            case PICKUP_MATCHING -> payload.identity() == null ? ActionReason.IDENTITY_MISSING : ActionReason.NO_MATCHING_SOURCE;
            case DROP_CARRIED -> menu.getCarried().isEmpty() ? ActionReason.SOURCE_MISSING : ActionReason.NO_VALID_DESTINATION;
            case DROP_CARRIED_TO_SLOT -> {
                if (menu.getCarried().isEmpty()) {
                    yield ActionReason.SOURCE_MISSING;
                }
                yield CraftingGridPlacementOperations.resolveMenuSlot(menu, payload.targetMenuSlot()) == null
                        ? ActionReason.TARGET_MISSING
                        : ActionReason.NO_VALID_DESTINATION;
            }
            case TRASH_CARRIED -> menu.getCarried().isEmpty() ? ActionReason.SOURCE_MISSING : ActionReason.UNSPECIFIED;
            case VOID_MATCHING_CARRIED -> payload.identity() == null ? ActionReason.IDENTITY_MISSING : ActionReason.NO_MATCHING_SOURCE;
        };
    }

    private static SlotServerDispatchResult dispatchPaneCraftPlacement(
            ActionRequest request,
            ServerPlayer player,
            AbstractContainerMenu menu,
            CraftingGridActionRequests.CraftSpec spec
    ) {
        InventoryHostDescriptor host = StorageViewResolver.resolve(null, menu, player.getInventory(), null);
        ChestLikeMenuLayout layout = host == null ? null : host.layout();
        if (layout == null || spec.sourcePane() == null) {
            return blocked(request, ActionReason.UNSUPPORTED_SOURCE, summaryKeyForCraftRoute(CraftingGridActionRequests.Route.PANE_IDENTITY_PLACE));
        }

        Slot targetSlot = resolveCraftTargetSlot(menu, spec.targetMenuSlot());
        if (targetSlot == null) {
            return blocked(request, ActionReason.TARGET_MISSING, summaryKeyForCraftRoute(CraftingGridActionRequests.Route.PANE_IDENTITY_PLACE));
        }
        CraftingMenuRefreshSupport.RefreshPlan refreshPlan = CraftingMenuRefreshSupport.resolve(menu, List.of(spec.targetMenuSlot()));
        if (!refreshPlan.supported()) {
            return blocked(request, ActionReason.COMPAT_UNAVAILABLE, summaryKeyForCraftRoute(CraftingGridActionRequests.Route.PANE_IDENTITY_PLACE));
        }

        try (MenuMutationTransaction transaction = MenuMutationTransaction.capture(menu)) {
            Map<UUID, CompoundTag> syncedContents = new LinkedHashMap<>();
            int movedCount = switch (spec.sourcePane()) {
                case OPEN_CONTAINER -> CraftingGridPlacementOperations.placeFromOpenContainer(
                        player,
                        menu,
                        layout,
                        targetSlot,
                        spec.targetMenuSlot(),
                        spec.identity()
                );
                case CARRIED -> CraftingGridPlacementOperations.placeFromCarriedSources(
                        player,
                        menu,
                        layout,
                        targetSlot,
                        spec.targetMenuSlot(),
                        spec.identity(),
                        syncedContents
                );
            };

            if (movedCount <= 0) {
                return blocked(request, ActionReason.NO_MATCHING_SOURCE, summaryKeyForCraftRoute(CraftingGridActionRequests.Route.PANE_IDENTITY_PLACE));
            }

            targetSlot.setChanged();
            if (refreshPlan.refresh(menu) != CraftingMenuRefreshSupport.RefreshResult.REFRESHED) {
                if (syncedContents.isEmpty()) {
                    return failed(request, ActionReason.COMPAT_ERROR, summaryKeyForCraftRoute(CraftingGridActionRequests.Route.PANE_IDENTITY_PLACE));
                }
                transaction.commit();
                SlotDebugLog.log(
                        "Craft grid place kept mutation after refresh failure: requestId={} menu={} targetMenuSlot={} sourcePane={}",
                        request.requestId(),
                        menu.getClass().getName(),
                        spec.targetMenuSlot(),
                        spec.sourcePane()
                );
                menu.broadcastChanges();
                return confirmed(request, movedCount, syncedContents, summaryKeyForCraftRoute(CraftingGridActionRequests.Route.PANE_IDENTITY_PLACE));
            }
            transaction.commit();
            return confirmed(request, movedCount, syncedContents);
        }
    }

    private static SlotServerDispatchResult dispatchCursorCraftPlacement(
            ActionRequest request,
            AbstractContainerMenu menu,
            CraftingGridActionRequests.CraftSpec spec
    ) {
        if (spec.cursorMode() == null) {
            return blocked(request, ActionReason.UNSUPPORTED_SOURCE, summaryKeyForCraftRoute(CraftingGridActionRequests.Route.CURSOR_PLACE));
        }

        if (!ItemBehaviorPolicy.matchesMovableIdentity(menu.getCarried(), spec.identity())) {
            return blocked(request, ActionReason.CURSOR_STATE_MISMATCH, summaryKeyForCraftRoute(CraftingGridActionRequests.Route.CURSOR_PLACE));
        }

        Slot targetSlot = resolveCraftTargetSlot(menu, spec.targetMenuSlot());
        if (targetSlot == null) {
            return blocked(request, ActionReason.TARGET_MISSING, summaryKeyForCraftRoute(CraftingGridActionRequests.Route.CURSOR_PLACE));
        }
        CraftingMenuRefreshSupport.RefreshPlan refreshPlan = CraftingMenuRefreshSupport.resolve(menu, List.of(spec.targetMenuSlot()));
        if (!refreshPlan.supported()) {
            return blocked(request, ActionReason.COMPAT_UNAVAILABLE, summaryKeyForCraftRoute(CraftingGridActionRequests.Route.CURSOR_PLACE));
        }

        try (MenuMutationTransaction transaction = MenuMutationTransaction.capture(menu)) {
            int movedCount = CraftingGridPlacementOperations.placeFromCursorCarried(
                    menu,
                    targetSlot,
                    spec.targetMenuSlot(),
                    spec.cursorMode() == CraftingGridActionRequests.CursorMode.ONE
            );
            if (movedCount <= 0) {
                return blocked(request, ActionReason.NO_VALID_DESTINATION, summaryKeyForCraftRoute(CraftingGridActionRequests.Route.CURSOR_PLACE));
            }

            targetSlot.setChanged();
            if (refreshPlan.refresh(menu) != CraftingMenuRefreshSupport.RefreshResult.REFRESHED) {
                return failed(request, ActionReason.COMPAT_ERROR, summaryKeyForCraftRoute(CraftingGridActionRequests.Route.CURSOR_PLACE));
            }
            transaction.commit();
            return confirmed(request, movedCount, Map.of());
        }
    }

    private static SlotServerDispatchResult dispatchCursorCraftDistribution(
            ActionRequest request,
            AbstractContainerMenu menu,
            CraftingGridActionRequests.CraftSpec spec
    ) {
        if (spec.cursorMode() == null || spec.targetMenuSlots().isEmpty()) {
            return blocked(request, ActionReason.UNSUPPORTED_SOURCE, summaryKeyForCraftRoute(CraftingGridActionRequests.Route.CURSOR_DISTRIBUTE));
        }

        if (!ItemBehaviorPolicy.matchesMovableIdentity(menu.getCarried(), spec.identity())) {
            return blocked(request, ActionReason.CURSOR_STATE_MISMATCH, summaryKeyForCraftRoute(CraftingGridActionRequests.Route.CURSOR_DISTRIBUTE));
        }
        if (spec.targetMenuSlots().size() > menu.getCarried().getCount()) {
            return blocked(request, ActionReason.CURSOR_STATE_MISMATCH, summaryKeyForCraftRoute(CraftingGridActionRequests.Route.CURSOR_DISTRIBUTE));
        }
        CraftingMenuRefreshSupport.RefreshPlan refreshPlan = CraftingMenuRefreshSupport.resolve(menu, spec.targetMenuSlots());
        if (!refreshPlan.supported()) {
            return blocked(request, ActionReason.COMPAT_UNAVAILABLE, summaryKeyForCraftRoute(CraftingGridActionRequests.Route.CURSOR_DISTRIBUTE));
        }

        try (MenuMutationTransaction transaction = MenuMutationTransaction.capture(menu)) {
            int movedCount = CraftingGridPlacementOperations.distributeFromCursorCarried(
                    menu,
                    spec.targetMenuSlots(),
                    spec.cursorMode() == CraftingGridActionRequests.CursorMode.ONE
            );
            if (movedCount <= 0) {
                return blocked(request, ActionReason.NO_VALID_DESTINATION, summaryKeyForCraftRoute(CraftingGridActionRequests.Route.CURSOR_DISTRIBUTE));
            }

            if (refreshPlan.refresh(menu) != CraftingMenuRefreshSupport.RefreshResult.REFRESHED) {
                return failed(request, ActionReason.COMPAT_ERROR, summaryKeyForCraftRoute(CraftingGridActionRequests.Route.CURSOR_DISTRIBUTE));
            }
            transaction.commit();
            return confirmed(request, movedCount, Map.of());
        }
    }

    private static SlotServerDispatchResult dispatchCraftResultExtraction(
            ActionRequest request,
            ServerPlayer player,
            AbstractContainerMenu menu,
            CraftingGridActionRequests.CraftSpec spec,
            List<ExternalToolSpec> craftingTools
    ) {
        if (spec.resultAction() == null || spec.repeatCount() <= 0) {
            return blocked(request, ActionReason.UNSUPPORTED_SOURCE, summaryKeyForCraftRoute(CraftingGridActionRequests.Route.RESULT_EXTRACT));
        }

        Slot resultSlot = resolveCraftTargetSlot(menu, spec.targetMenuSlot());
        if (resultSlot == null) {
            return blocked(request, ActionReason.TARGET_MISSING, summaryKeyForCraftRoute(CraftingGridActionRequests.Route.RESULT_EXTRACT));
        }
        if (!resultSlot.hasItem()) {
            return blocked(request, ActionReason.SOURCE_MISSING, summaryKeyForCraftRoute(CraftingGridActionRequests.Route.RESULT_EXTRACT));
        }
        List<Integer> inputSlots = craftingTools.stream()
                .flatMap(tool -> tool.menuSlotsForRole(ExternalToolSlotRole.INPUT).stream())
                .distinct()
                .toList();
        CraftingMenuRefreshSupport.RefreshPlan refreshPlan = CraftingMenuRefreshSupport.resolve(menu, inputSlots);
        if (!refreshPlan.supported()) {
            return blocked(request, ActionReason.COMPAT_UNAVAILABLE, summaryKeyForCraftRoute(CraftingGridActionRequests.Route.RESULT_EXTRACT));
        }

        try (MenuMutationTransaction transaction = MenuMutationTransaction.capture(menu)) {
            CraftingResultOperations.ExtractionResult extractionResult = CraftingResultOperations.extractResult(
                    menu,
                    player,
                    spec.targetMenuSlot(),
                    spec.resultAction(),
                    spec.mouseButton(),
                    spec.repeatCount()
            );
            if (extractionResult.extractedCount() <= 0) {
                return blocked(request, ActionReason.NO_VALID_DESTINATION, summaryKeyForCraftRoute(CraftingGridActionRequests.Route.RESULT_EXTRACT));
            }

            if (refreshPlan.refresh(menu) != CraftingMenuRefreshSupport.RefreshResult.REFRESHED) {
                return failed(request, ActionReason.COMPAT_ERROR, summaryKeyForCraftRoute(CraftingGridActionRequests.Route.RESULT_EXTRACT));
            }
            transaction.commit();
            return confirmed(
                    request,
                    extractionResult.extractedCount(),
                    Map.of(),
                    summaryKeyForCraftRoute(CraftingGridActionRequests.Route.RESULT_EXTRACT),
                    AcquisitionProducerId.CRAFT_RESULT,
                    acquisitionItemIds(extractionResult.acquiredItemId())
            );
        }
    }

    private static Slot resolveCraftTargetSlot(AbstractContainerMenu menu, int targetMenuSlot) {
        Slot targetSlot = CraftingGridPlacementOperations.resolveMenuSlot(menu, targetMenuSlot);
        if (targetSlot != null) {
            return targetSlot;
        }

        SlotDebugLog.log(
                "Craft grid place rejected invalid target slot: menu={} targetMenuSlot={} rawMenuSlots={}",
                menu.getClass().getName(),
                targetMenuSlot,
                menu.slots.size()
        );
        return null;
    }

    private static SlotServerDispatchResult blocked(ActionRequest request, ActionReason reason) {
        return blocked(request, reason, "");
    }

    private static SlotServerDispatchResult failed(ActionRequest request, ActionReason reason, String summaryKey) {
        return SlotServerDispatchResult.of(
                new ActionOutcome(
                        request == null ? null : request.requestId(),
                        request == null ? ActionFamily.TRANSFER : request.actionFamily(),
                        dev.imagio.slot.operation.ActionStatus.FAILED,
                        reason == null ? ActionReason.INTERNAL_ERROR : reason,
                        0,
                        RefreshScope.SESSION,
                        List.of(),
                        List.of(),
                        "",
                        summaryKey == null ? "" : summaryKey
                ),
                false,
                Map.of()
        );
    }

    private static SlotServerDispatchResult blocked(ActionRequest request, ActionReason reason, String summaryKey) {
        return SlotServerDispatchResult.of(
                new ActionOutcome(
                        request == null ? null : request.requestId(),
                        request == null ? ActionFamily.TRANSFER : request.actionFamily(),
                        dev.imagio.slot.operation.ActionStatus.BLOCKED,
                        reason,
                        0,
                        RefreshScope.NONE,
                        List.of(),
                        List.of(),
                        "",
                        summaryKey == null ? "" : summaryKey
                ),
                false,
                Map.of()
        );
    }

    private static boolean requiresCraftIdentity(CraftingGridActionRequests.Route route) {
        return route == CraftingGridActionRequests.Route.PANE_IDENTITY_PLACE
                || route == CraftingGridActionRequests.Route.CURSOR_PLACE
                || route == CraftingGridActionRequests.Route.CURSOR_DISTRIBUTE;
    }

    private static ExternalToolSlotRole requiredCraftTargetRole(CraftingGridActionRequests.Route route) {
        return route == CraftingGridActionRequests.Route.RESULT_EXTRACT
                ? ExternalToolSlotRole.OUTPUT
                : ExternalToolSlotRole.INPUT;
    }

    private static String summaryKeyForCraftRoute(CraftingGridActionRequests.Route route) {
        return route == CraftingGridActionRequests.Route.RESULT_EXTRACT
                ? "slot.screen.action.outcome.craft.extract"
                : "slot.screen.action.outcome.craft";
    }

    private static List<String> acquisitionItemIds(String itemId) {
        return itemId == null || itemId.isBlank() ? List.of() : List.of(itemId);
    }
}
