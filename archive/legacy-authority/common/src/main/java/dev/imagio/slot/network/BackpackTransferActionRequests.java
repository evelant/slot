package dev.imagio.slot.network;

import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.intent.ActionFamily;
import dev.imagio.slot.intent.ActionRequest;
import dev.imagio.slot.intent.ActionRequestId;
import dev.imagio.slot.source.SourceId;
import dev.imagio.slot.source.SourceSlotRef;

public final class BackpackTransferActionRequests {
    private static final String AGGREGATE_PAYLOAD = ActionRequestSourceIds.AGGREGATE_PAYLOAD;
    private static final String KIND_MENU_SLOT = "menu_slot";
    private static final String KIND_MENU_SLOT_REPLACE = "menu_slot_replace";
    private static final String KIND_PLAYER_OFFHAND = "player_offhand";
    private static final String KIND_PLAYER_OFFHAND_REPLACE = "player_offhand_replace";

    public static final String SOURCE_OPEN_CONTAINER = ActionRequestSourceIds.SOURCE_OPEN_CONTAINER;
    public static final String SOURCE_PLAYER_BACKPACK = ActionRequestSourceIds.SOURCE_PLAYER_BACKPACK;
    public static final String SOURCE_PLAYER_OFFHAND = ActionRequestSourceIds.SOURCE_PLAYER_OFFHAND;
    public static final String SOURCE_CARRIED = ActionRequestSourceIds.SOURCE_CARRIED;
    public static final String SOURCE_MENU = ActionRequestSourceIds.SOURCE_MENU;

    public static final String KIND_EXTERNAL_IDENTITY = "external_identity";
    public static final String KIND_BACKPACK_IDENTITY = "backpack_identity";
    public static final String KIND_CARRIED_IDENTITY = "carried_identity";

    private BackpackTransferActionRequests() {
    }

    public static ActionRequest externalToCarried(
            int containerId,
            String expectedSessionFingerprint,
            ItemIdentity identity,
            BackpackTransferPayload.Mode mode
    ) {
        return request(
                ActionFamily.TRANSFER,
                containerId,
                expectedSessionFingerprint,
                new SourceSlotRef(KIND_EXTERNAL_IDENTITY, SourceId.of(SOURCE_OPEN_CONTAINER), AGGREGATE_PAYLOAD),
                null,
                identity,
                requestedCount(mode, 0)
        );
    }

    public static ActionRequest externalToCarried(
            int containerId,
            String expectedSessionFingerprint,
            ItemIdentity identity,
            int requestedCount
    ) {
        return request(
                ActionFamily.TRANSFER,
                containerId,
                expectedSessionFingerprint,
                new SourceSlotRef(KIND_EXTERNAL_IDENTITY, SourceId.of(SOURCE_OPEN_CONTAINER), AGGREGATE_PAYLOAD),
                null,
                identity,
                requestedCount
        );
    }

    public static ActionRequest menuToExternal(
            int containerId,
            String expectedSessionFingerprint,
            int menuSlot,
            BackpackTransferPayload.Mode mode
    ) {
        return request(
                ActionFamily.STORE,
                containerId,
                expectedSessionFingerprint,
                SourceSlotRef.menuSlot(SourceId.of(SOURCE_MENU), menuSlot),
                null,
                null,
                requestedCount(mode, 0)
        );
    }

    public static ActionRequest menuToExternal(
            int containerId,
            String expectedSessionFingerprint,
            int menuSlot,
            int requestedCount
    ) {
        return request(
                ActionFamily.STORE,
                containerId,
                expectedSessionFingerprint,
                SourceSlotRef.menuSlot(SourceId.of(SOURCE_MENU), menuSlot),
                null,
                null,
                requestedCount
        );
    }

    public static ActionRequest backpackToExternal(
            int containerId,
            String expectedSessionFingerprint,
            ItemIdentity identity,
            BackpackTransferPayload.Mode mode
    ) {
        return request(
                ActionFamily.STORE,
                containerId,
                expectedSessionFingerprint,
                new SourceSlotRef(KIND_BACKPACK_IDENTITY, SourceId.of(SOURCE_PLAYER_BACKPACK), AGGREGATE_PAYLOAD),
                null,
                identity,
                requestedCount(mode, 0)
        );
    }

    public static ActionRequest carriedToExternal(
            int containerId,
            String expectedSessionFingerprint,
            ItemIdentity identity,
            int requestedCount
    ) {
        return request(
                ActionFamily.STORE,
                containerId,
                expectedSessionFingerprint,
                new SourceSlotRef(KIND_CARRIED_IDENTITY, SourceId.of(SOURCE_CARRIED), AGGREGATE_PAYLOAD),
                null,
                identity,
                requestedCount
        );
    }

    public static ActionRequest backpackToMenu(
            int containerId,
            String expectedSessionFingerprint,
            ItemIdentity identity,
            int menuSlot,
            int requestedCount
    ) {
        return backpackToMenu(
                containerId,
                expectedSessionFingerprint,
                identity,
                menuSlot,
                requestedCount,
                TargetPolicy.FILL_ONLY
        );
    }

    public static ActionRequest backpackToMenu(
            int containerId,
            String expectedSessionFingerprint,
            ItemIdentity identity,
            int menuSlot,
            int requestedCount,
            TargetPolicy targetPolicy
    ) {
        return request(
                ActionFamily.STORE,
                containerId,
                expectedSessionFingerprint,
                new SourceSlotRef(KIND_BACKPACK_IDENTITY, SourceId.of(SOURCE_PLAYER_BACKPACK), AGGREGATE_PAYLOAD),
                targetRef(menuSlot, targetPolicy),
                identity,
                requestedCount
        );
    }

    public static ActionRequest backpackToOffhand(
            int containerId,
            String expectedSessionFingerprint,
            ItemIdentity identity,
            int requestedCount
    ) {
        return backpackToOffhand(
                containerId,
                expectedSessionFingerprint,
                identity,
                requestedCount,
                TargetPolicy.FILL_ONLY
        );
    }

    public static ActionRequest backpackToOffhand(
            int containerId,
            String expectedSessionFingerprint,
            ItemIdentity identity,
            int requestedCount,
            TargetPolicy targetPolicy
    ) {
        return request(
                ActionFamily.STORE,
                containerId,
                expectedSessionFingerprint,
                new SourceSlotRef(KIND_BACKPACK_IDENTITY, SourceId.of(SOURCE_PLAYER_BACKPACK), AGGREGATE_PAYLOAD),
                offhandTargetRef(targetPolicy),
                identity,
                requestedCount
        );
    }

    public static ActionRequest fromLegacyPayload(BackpackTransferPayload payload) {
        if (payload == null) {
            return null;
        }

        return switch (payload.direction()) {
            case EXTERNAL_TO_CARRIED -> payload.requestedCount() > 0
                    ? externalToCarried(payload.containerId(), "", payload.identity(), payload.requestedCount())
                    : externalToCarried(payload.containerId(), "", payload.identity(), payload.mode());
            case MENU_TO_EXTERNAL -> menuToExternal(payload.containerId(), "", payload.menuSlot(), payload.mode());
            case CARRIED_TO_EXTERNAL -> carriedToExternal(payload.containerId(), "", payload.identity(), payload.requestedCount());
            case BACKPACK_TO_EXTERNAL -> backpackToExternal(payload.containerId(), "", payload.identity(), payload.mode());
            case BACKPACK_TO_MENU -> backpackToMenu(payload.containerId(), "", payload.identity(), payload.menuSlot(), payload.requestedCount());
        };
    }

    public static LegacyResolution resolve(ActionRequest request) {
        if (request == null || request.primarySourceRef() == null) {
            return null;
        }

        SourceSlotRef primary = request.primarySourceRef();
        ItemIdentity identity = ActionRequestIdentityCodec.decode(request.identityKey());
        if (request.actionFamily() == ActionFamily.TRANSFER
                && KIND_EXTERNAL_IDENTITY.equals(primary.kind())
                && SOURCE_OPEN_CONTAINER.equals(primary.sourceId().value())
                && identity != null) {
            return new LegacyResolution(
                    Route.EXTERNAL_TO_CARRIED,
                    spec(
                            request.expectedContainerId(),
                            request.requestedCount(),
                            -1,
                            identity
                    )
            );
        }

        if (request.actionFamily() != ActionFamily.STORE) {
            return null;
        }

        if ("menu_slot".equals(primary.kind())) {
            int menuSlot = parseMenuSlot(primary);
            if (menuSlot < 0) {
                return null;
            }
            return new LegacyResolution(
                    Route.MENU_TO_EXTERNAL,
                    new LegacyTransferSpec(
                            request.expectedContainerId(),
                            request.requestedCount(),
                            menuSlot,
                            "",
                            dev.imagio.slot.client.model.ComparisonMode.ITEM_ID,
                            "",
                            TargetType.MENU_SLOT,
                            TargetPolicy.FILL_ONLY
                    )
            );
        }

        if (KIND_CARRIED_IDENTITY.equals(primary.kind()) && identity != null) {
            return new LegacyResolution(
                    Route.CARRIED_TO_EXTERNAL,
                    spec(
                            request.expectedContainerId(),
                            request.requestedCount(),
                            -1,
                            identity
                    )
            );
        }

        if (KIND_BACKPACK_IDENTITY.equals(primary.kind()) && identity != null) {
            SourceSlotRef secondary = request.secondarySourceRef();
            TargetDescriptor target = targetDescriptor(secondary);
            if (target != null) {
                if (target.targetType() == TargetType.MENU_SLOT && target.menuSlot() < 0) {
                    return null;
                }
                return new LegacyResolution(
                        Route.BACKPACK_TO_MENU,
                        spec(
                                request.expectedContainerId(),
                                request.requestedCount(),
                                target.menuSlot(),
                                identity,
                                target.targetType(),
                                target.targetPolicy()
                        )
                );
            }

            return new LegacyResolution(
                    Route.BACKPACK_TO_EXTERNAL,
                    spec(
                            request.expectedContainerId(),
                            request.requestedCount(),
                            -1,
                            identity
                    )
            );
        }

        return null;
    }

    private static LegacyTransferSpec spec(
            int containerId,
            int requestedCount,
            int menuSlot,
            ItemIdentity identity
    ) {
        return spec(containerId, requestedCount, menuSlot, identity, TargetType.MENU_SLOT, TargetPolicy.FILL_ONLY);
    }

    private static LegacyTransferSpec spec(
            int containerId,
            int requestedCount,
            int menuSlot,
            ItemIdentity identity,
            TargetType targetType,
            TargetPolicy targetPolicy
    ) {
        ItemIdentity resolvedIdentity = identity == null ? ItemIdentity.of("minecraft:air") : identity;
        return new LegacyTransferSpec(
                containerId,
                requestedCount,
                menuSlot,
                resolvedIdentity.itemId(),
                resolvedIdentity.comparisonMode(),
                resolvedIdentity.componentFingerprint(),
                targetType == null ? TargetType.MENU_SLOT : targetType,
                targetPolicy == null ? TargetPolicy.FILL_ONLY : targetPolicy
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

    private static int requestedCount(BackpackTransferPayload.Mode mode, int explicitRequestedCount) {
        if (explicitRequestedCount > 0) {
            return explicitRequestedCount;
        }
        if (mode == null) {
            return 0;
        }
        return switch (mode) {
            case ONE -> 1;
            case STACK -> 0;
            case ALL -> Integer.MAX_VALUE;
        };
    }

    private static BackpackTransferPayload.Mode modeForRequestedCount(int requestedCount) {
        if (requestedCount == 1) {
            return BackpackTransferPayload.Mode.ONE;
        }
        if (requestedCount == Integer.MAX_VALUE) {
            return BackpackTransferPayload.Mode.ALL;
        }
        return BackpackTransferPayload.Mode.STACK;
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

    private static SourceSlotRef targetRef(int menuSlot, TargetPolicy targetPolicy) {
        return new SourceSlotRef(
                targetPolicy == TargetPolicy.REPLACE_EXISTING ? KIND_MENU_SLOT_REPLACE : KIND_MENU_SLOT,
                SourceId.of(SOURCE_MENU),
                Integer.toString(menuSlot)
        );
    }

    private static SourceSlotRef offhandTargetRef(TargetPolicy targetPolicy) {
        return new SourceSlotRef(
                targetPolicy == TargetPolicy.REPLACE_EXISTING ? KIND_PLAYER_OFFHAND_REPLACE : KIND_PLAYER_OFFHAND,
                SourceId.of(SOURCE_PLAYER_OFFHAND),
                AGGREGATE_PAYLOAD
        );
    }

    private static TargetPolicy targetPolicy(SourceSlotRef ref) {
        if (ref == null) {
            return null;
        }
        return switch (ref.kind()) {
            case KIND_MENU_SLOT -> TargetPolicy.FILL_ONLY;
            case KIND_MENU_SLOT_REPLACE -> TargetPolicy.REPLACE_EXISTING;
            case KIND_PLAYER_OFFHAND -> TargetPolicy.FILL_ONLY;
            case KIND_PLAYER_OFFHAND_REPLACE -> TargetPolicy.REPLACE_EXISTING;
            default -> null;
        };
    }

    private static TargetDescriptor targetDescriptor(SourceSlotRef ref) {
        if (ref == null) {
            return null;
        }

        TargetPolicy targetPolicy = targetPolicy(ref);
        if (targetPolicy == null) {
            return null;
        }

        return switch (ref.kind()) {
            case KIND_MENU_SLOT, KIND_MENU_SLOT_REPLACE -> new TargetDescriptor(TargetType.MENU_SLOT, parseMenuSlot(ref), targetPolicy);
            case KIND_PLAYER_OFFHAND, KIND_PLAYER_OFFHAND_REPLACE -> new TargetDescriptor(TargetType.PLAYER_OFFHAND, -1, targetPolicy);
            default -> null;
        };
    }

    public record LegacyResolution(Route route, LegacyTransferSpec spec) {
        public BackpackTransferPayload payload() {
            return new BackpackTransferPayload(
                    spec.containerId(),
                    switch (route) {
                        case EXTERNAL_TO_CARRIED -> BackpackTransferPayload.Direction.EXTERNAL_TO_CARRIED;
                        case MENU_TO_EXTERNAL -> BackpackTransferPayload.Direction.MENU_TO_EXTERNAL;
                        case CARRIED_TO_EXTERNAL -> BackpackTransferPayload.Direction.CARRIED_TO_EXTERNAL;
                        case BACKPACK_TO_EXTERNAL -> BackpackTransferPayload.Direction.BACKPACK_TO_EXTERNAL;
                        case BACKPACK_TO_MENU -> BackpackTransferPayload.Direction.BACKPACK_TO_MENU;
                    },
                    modeForRequestedCount(spec.requestedCount()),
                    spec.requestedCount(),
                    spec.menuSlot(),
                    spec.itemId(),
                    spec.comparisonMode(),
                    spec.componentFingerprint()
            );
        }
    }

    public record LegacyTransferSpec(
            int containerId,
            int requestedCount,
            int menuSlot,
            String itemId,
            dev.imagio.slot.client.model.ComparisonMode comparisonMode,
            String componentFingerprint,
            TargetType targetType,
            TargetPolicy targetPolicy
    ) {
    }

    public enum Route {
        EXTERNAL_TO_CARRIED,
        MENU_TO_EXTERNAL,
        CARRIED_TO_EXTERNAL,
        BACKPACK_TO_EXTERNAL,
        BACKPACK_TO_MENU
    }

    public enum TargetType {
        MENU_SLOT,
        PLAYER_OFFHAND
    }

    public enum TargetPolicy {
        FILL_ONLY,
        REPLACE_EXISTING
    }

    private record TargetDescriptor(
            TargetType targetType,
            int menuSlot,
            TargetPolicy targetPolicy
    ) {
    }
}
