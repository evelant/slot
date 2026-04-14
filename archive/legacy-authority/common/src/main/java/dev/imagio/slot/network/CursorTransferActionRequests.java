package dev.imagio.slot.network;

import dev.imagio.slot.client.model.ComparisonMode;
import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.projection.InventoryPane;
import dev.imagio.slot.intent.ActionFamily;
import dev.imagio.slot.intent.ActionRequest;
import dev.imagio.slot.intent.ActionRequestId;
import dev.imagio.slot.source.SourceId;
import dev.imagio.slot.source.SourceSlotRef;

public final class CursorTransferActionRequests {
    private static final int REQUESTED_COUNT_ONE = 1;
    private static final int REQUESTED_COUNT_HALF = 2;
    private static final int REQUESTED_COUNT_STACK = Integer.MAX_VALUE;

    public static final String SOURCE_OPEN_CONTAINER = ActionRequestSourceIds.SOURCE_OPEN_CONTAINER;
    public static final String SOURCE_CARRIED = ActionRequestSourceIds.SOURCE_CARRIED;
    public static final String SOURCE_MENU = ActionRequestSourceIds.SOURCE_MENU;

    public static final String KIND_PANE_IDENTITY = "pane_identity";
    public static final String KIND_CARRIED_CURSOR = "carried_cursor";
    public static final String KIND_PANE_TARGET = "pane_target";

    private CursorTransferActionRequests() {
    }

    public static ActionRequest pickupMatching(
            int containerId,
            String expectedSessionFingerprint,
            InventoryPane pane,
            ItemIdentity identity,
            Mode mode
    ) {
        return request(
                ActionFamily.PICKUP,
                containerId,
                expectedSessionFingerprint,
                new SourceSlotRef(KIND_PANE_IDENTITY, SourceId.of(sourceIdForPane(pane)), ActionRequestSourceIds.AGGREGATE_PAYLOAD),
                null,
                identity,
                requestedCount(mode)
        );
    }

    public static ActionRequest dropCarried(
            int containerId,
            String expectedSessionFingerprint,
            InventoryPane pane,
            Mode mode
    ) {
        return request(
                ActionFamily.DROP,
                containerId,
                expectedSessionFingerprint,
                carriedCursorRef(),
                new SourceSlotRef(KIND_PANE_TARGET, SourceId.of(sourceIdForPane(pane)), ActionRequestSourceIds.AGGREGATE_PAYLOAD),
                null,
                requestedCount(mode)
        );
    }

    public static ActionRequest dropCarriedToMenuSlot(
            int containerId,
            String expectedSessionFingerprint,
            int targetMenuSlot,
            Mode mode
    ) {
        return request(
                ActionFamily.DROP,
                containerId,
                expectedSessionFingerprint,
                carriedCursorRef(),
                SourceSlotRef.menuSlot(SourceId.of(SOURCE_MENU), targetMenuSlot),
                null,
                requestedCount(mode)
        );
    }

    public static ActionRequest trashCarried(
            int containerId,
            String expectedSessionFingerprint,
            Mode mode
    ) {
        return request(
                ActionFamily.TRASH,
                containerId,
                expectedSessionFingerprint,
                carriedCursorRef(),
                null,
                null,
                requestedCount(mode)
        );
    }

    public static ActionRequest voidMatchingCarried(
            int containerId,
            String expectedSessionFingerprint,
            ItemIdentity identity,
            Mode mode
    ) {
        return request(
                ActionFamily.VOID,
                containerId,
                expectedSessionFingerprint,
                new SourceSlotRef(KIND_PANE_IDENTITY, SourceId.of(SOURCE_CARRIED), ActionRequestSourceIds.AGGREGATE_PAYLOAD),
                null,
                identity,
                requestedCount(mode)
        );
    }

    public static ActionRequest fromLegacyPayload(CursorTransferPayload payload) {
        if (payload == null) {
            return null;
        }

        return switch (payload.action()) {
            case PICKUP_MATCHING -> pickupMatching(
                    payload.containerId(),
                    "",
                    toPane(payload.targetPane()),
                    payload.identity(),
                    mode(payload.mode())
            );
            case DROP_CARRIED -> dropCarried(
                    payload.containerId(),
                    "",
                    toPane(payload.targetPane()),
                    mode(payload.mode())
            );
            case DROP_CARRIED_TO_SLOT -> dropCarriedToMenuSlot(
                    payload.containerId(),
                    "",
                    payload.targetMenuSlot(),
                    mode(payload.mode())
            );
            case TRASH_CARRIED -> trashCarried(payload.containerId(), "", mode(payload.mode()));
            case VOID_MATCHING_CARRIED -> voidMatchingCarried(
                    payload.containerId(),
                    "",
                    payload.identity(),
                    mode(payload.mode())
            );
        };
    }

    public static LegacyResolution resolve(ActionRequest request) {
        if (request == null || request.primarySourceRef() == null) {
            return null;
        }

        SourceSlotRef primary = request.primarySourceRef();
        ItemIdentity identity = ActionRequestIdentityCodec.decode(request.identityKey());
        return switch (request.actionFamily()) {
            case PICKUP -> resolvePickup(request, primary, identity);
            case DROP -> resolveDrop(request, primary);
            case TRASH -> resolveTrash(request, primary);
            case VOID -> resolveVoid(request, primary, identity);
            default -> null;
        };
    }

    private static LegacyResolution resolvePickup(ActionRequest request, SourceSlotRef primary, ItemIdentity identity) {
        if (!KIND_PANE_IDENTITY.equals(primary.kind()) || identity == null) {
            return null;
        }

        InventoryPane pane = paneForSourceId(primary.sourceId().value());
        Mode mode = modeForRequestedCount(request.requestedCount(), true);
        if (pane == null || mode == null) {
            return null;
        }

        return new LegacyResolution(
                Route.PICKUP_MATCHING,
                spec(
                        request.expectedContainerId(),
                        pane,
                        mode,
                        -1,
                        identity
                )
        );
    }

    private static LegacyResolution resolveDrop(ActionRequest request, SourceSlotRef primary) {
        if (!isCarriedCursor(primary)) {
            return null;
        }

        Mode mode = modeForRequestedCount(request.requestedCount(), false);
        if (mode == null) {
            return null;
        }

        SourceSlotRef secondary = request.secondarySourceRef();
        if (secondary == null) {
            return null;
        }

        if ("menu_slot".equals(secondary.kind())) {
            int menuSlot = parseMenuSlot(secondary);
            if (menuSlot < 0) {
                return null;
            }
            return new LegacyResolution(
                    Route.DROP_CARRIED_TO_SLOT,
                    spec(
                            request.expectedContainerId(),
                            InventoryPane.CARRIED,
                            mode,
                            menuSlot,
                            null
                    )
            );
        }

        if (!KIND_PANE_TARGET.equals(secondary.kind())) {
            return null;
        }

        InventoryPane pane = paneForSourceId(secondary.sourceId().value());
        if (pane == null) {
            return null;
        }
        return new LegacyResolution(
                Route.DROP_CARRIED,
                spec(
                        request.expectedContainerId(),
                        pane,
                        mode,
                        -1,
                        null
                )
        );
    }

    private static LegacyResolution resolveTrash(ActionRequest request, SourceSlotRef primary) {
        if (!isCarriedCursor(primary)) {
            return null;
        }

        Mode mode = modeForRequestedCount(request.requestedCount(), false);
        if (mode == null) {
            return null;
        }

        return new LegacyResolution(
                Route.TRASH_CARRIED,
                spec(
                        request.expectedContainerId(),
                        InventoryPane.CARRIED,
                        mode,
                        -1,
                        null
                )
        );
    }

    private static LegacyResolution resolveVoid(ActionRequest request, SourceSlotRef primary, ItemIdentity identity) {
        if (!KIND_PANE_IDENTITY.equals(primary.kind())
                || !SOURCE_CARRIED.equals(primary.sourceId().value())
                || identity == null) {
            return null;
        }

        Mode mode = modeForRequestedCount(request.requestedCount(), false);
        if (mode == null) {
            return null;
        }

        return new LegacyResolution(
                Route.VOID_MATCHING_CARRIED,
                spec(
                        request.expectedContainerId(),
                        InventoryPane.CARRIED,
                        mode,
                        -1,
                        identity
                )
        );
    }

    private static LegacyCursorSpec spec(
            int containerId,
            InventoryPane pane,
            Mode mode,
            int targetMenuSlot,
            ItemIdentity identity
    ) {
        ItemIdentity resolvedIdentity = identity == null ? null : identity;
        return new LegacyCursorSpec(
                containerId,
                pane == null ? InventoryPane.CARRIED : pane,
                mode == null ? Mode.STACK : mode,
                targetMenuSlot,
                resolvedIdentity == null ? "" : resolvedIdentity.itemId(),
                resolvedIdentity == null ? ComparisonMode.ITEM_ID : resolvedIdentity.comparisonMode(),
                resolvedIdentity == null ? "" : resolvedIdentity.componentFingerprint()
        );
    }

    private static ActionRequest request(
            ActionFamily actionFamily,
            int containerId,
            String expectedSessionFingerprint,
            SourceSlotRef primarySourceRef,
            SourceSlotRef secondarySourceRef,
            ItemIdentity identity,
            int requestedCount
    ) {
        return new ActionRequest(
                ActionRequest.CURRENT_SCHEMA_VERSION,
                ActionRequestId.create(),
                expectedSessionFingerprint == null ? "" : expectedSessionFingerprint,
                containerId,
                actionFamily,
                primarySourceRef,
                secondarySourceRef,
                "",
                ActionRequestIdentityCodec.encode(identity),
                Math.max(0, requestedCount)
        );
    }

    private static boolean isCarriedCursor(SourceSlotRef ref) {
        return ref != null
                && KIND_CARRIED_CURSOR.equals(ref.kind())
                && SOURCE_CARRIED.equals(ref.sourceId().value());
    }

    private static SourceSlotRef carriedCursorRef() {
        return new SourceSlotRef(KIND_CARRIED_CURSOR, SourceId.of(SOURCE_CARRIED), ActionRequestSourceIds.AGGREGATE_PAYLOAD);
    }

    private static String sourceIdForPane(InventoryPane pane) {
        return pane == InventoryPane.OPEN_CONTAINER ? SOURCE_OPEN_CONTAINER : SOURCE_CARRIED;
    }

    private static InventoryPane toPane(CursorTransferPayload.TargetPane targetPane) {
        return targetPane == CursorTransferPayload.TargetPane.OPEN_CONTAINER
                ? InventoryPane.OPEN_CONTAINER
                : InventoryPane.CARRIED;
    }

    private static InventoryPane paneForSourceId(String sourceId) {
        if (SOURCE_OPEN_CONTAINER.equals(sourceId)) {
            return InventoryPane.OPEN_CONTAINER;
        }
        if (SOURCE_CARRIED.equals(sourceId)) {
            return InventoryPane.CARRIED;
        }
        return null;
    }

    private static int requestedCount(Mode mode) {
        if (mode == null) {
            return REQUESTED_COUNT_STACK;
        }
        return switch (mode) {
            case ONE -> REQUESTED_COUNT_ONE;
            // Half is source-dependent; reserve a sentinel so the dispatcher can reconstruct it exactly.
            case HALF -> REQUESTED_COUNT_HALF;
            case STACK -> REQUESTED_COUNT_STACK;
        };
    }

    private static Mode modeForRequestedCount(int requestedCount, boolean allowHalf) {
        if (requestedCount <= 0) {
            return null;
        }
        if (requestedCount == REQUESTED_COUNT_ONE) {
            return Mode.ONE;
        }
        if (allowHalf && requestedCount == REQUESTED_COUNT_HALF) {
            return Mode.HALF;
        }
        if (!allowHalf && requestedCount == REQUESTED_COUNT_HALF) {
            return null;
        }
        return Mode.STACK;
    }

    private static CursorTransferPayload.TargetPane targetPane(InventoryPane pane) {
        return pane == InventoryPane.OPEN_CONTAINER
                ? CursorTransferPayload.TargetPane.OPEN_CONTAINER
                : CursorTransferPayload.TargetPane.CARRIED;
    }

    private static CursorTransferPayload.Mode payloadMode(Mode mode) {
        return switch (mode) {
            case ONE -> CursorTransferPayload.Mode.ONE;
            case HALF -> CursorTransferPayload.Mode.HALF;
            case STACK -> CursorTransferPayload.Mode.STACK;
        };
    }

    private static Mode mode(CursorTransferPayload.Mode payloadMode) {
        if (payloadMode == null) {
            return Mode.STACK;
        }
        return switch (payloadMode) {
            case ONE -> Mode.ONE;
            case HALF -> Mode.HALF;
            case STACK -> Mode.STACK;
        };
    }

    private static int parseMenuSlot(SourceSlotRef ref) {
        if (ref == null) {
            return -1;
        }
        try {
            return Integer.parseInt(ref.payload());
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    public record LegacyResolution(Route route, LegacyCursorSpec spec) {
        public CursorTransferPayload payload() {
            return new CursorTransferPayload(
                    spec.containerId(),
                    switch (route) {
                        case PICKUP_MATCHING -> CursorTransferPayload.Action.PICKUP_MATCHING;
                        case DROP_CARRIED -> CursorTransferPayload.Action.DROP_CARRIED;
                        case DROP_CARRIED_TO_SLOT -> CursorTransferPayload.Action.DROP_CARRIED_TO_SLOT;
                        case TRASH_CARRIED -> CursorTransferPayload.Action.TRASH_CARRIED;
                        case VOID_MATCHING_CARRIED -> CursorTransferPayload.Action.VOID_MATCHING_CARRIED;
                    },
                    targetPane(spec.pane()),
                    payloadMode(spec.mode()),
                    spec.targetMenuSlot(),
                    spec.itemId(),
                    spec.comparisonMode(),
                    spec.componentFingerprint()
            );
        }
    }

    public record LegacyCursorSpec(
            int containerId,
            InventoryPane pane,
            Mode mode,
            int targetMenuSlot,
            String itemId,
            ComparisonMode comparisonMode,
            String componentFingerprint
    ) {
    }

    public enum Mode {
        ONE,
        HALF,
        STACK
    }

    public enum Route {
        PICKUP_MATCHING,
        DROP_CARRIED,
        DROP_CARRIED_TO_SLOT,
        TRASH_CARRIED,
        VOID_MATCHING_CARRIED
    }
}
