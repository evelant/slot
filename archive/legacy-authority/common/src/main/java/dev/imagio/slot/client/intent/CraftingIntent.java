package dev.imagio.slot.client.intent;

import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.projection.InventoryPane;
import dev.imagio.slot.client.screen.container.MenuSlotId;
import dev.imagio.slot.network.ActionRequestClientContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public sealed interface CraftingIntent permits CraftingIntent.PlaceOne, CraftingIntent.PlaceCursor, CraftingIntent.DistributeCursor, CraftingIntent.ExtractResult {
    String expectedSessionFingerprint();

    int expectedContainerId();

    record PlaceOne(
            String expectedSessionFingerprint,
            int expectedContainerId,
            MenuSlotId targetMenuSlotId,
            ItemIdentity identity,
            InventoryPane sourcePane
    ) implements CraftingIntent {
        public PlaceOne {
            expectedSessionFingerprint = expectedSessionFingerprint == null ? "" : expectedSessionFingerprint;
            targetMenuSlotId = targetMenuSlotId == null ? MenuSlotId.of(-1) : targetMenuSlotId;
            Objects.requireNonNull(sourcePane, "sourcePane");
        }

        public static PlaceOne forCurrentSession(
                int containerId,
                MenuSlotId targetMenuSlotId,
                ItemIdentity identity,
                InventoryPane sourcePane
        ) {
            return new PlaceOne(
                    ActionRequestClientContext.currentSessionFingerprint(containerId),
                    containerId,
                    targetMenuSlotId,
                    identity,
                    sourcePane
            );
        }
    }

    record PlaceCursor(
            String expectedSessionFingerprint,
            int expectedContainerId,
            MenuSlotId targetMenuSlotId,
            ItemIdentity carriedIdentity,
            CursorMode cursorMode
    ) implements CraftingIntent {
        public PlaceCursor {
            expectedSessionFingerprint = expectedSessionFingerprint == null ? "" : expectedSessionFingerprint;
            targetMenuSlotId = targetMenuSlotId == null ? MenuSlotId.of(-1) : targetMenuSlotId;
            Objects.requireNonNull(cursorMode, "cursorMode");
        }

        public static PlaceCursor forCurrentSession(
                int containerId,
                MenuSlotId targetMenuSlotId,
                ItemIdentity carriedIdentity,
                CursorMode cursorMode
        ) {
            return new PlaceCursor(
                    ActionRequestClientContext.currentSessionFingerprint(containerId),
                    containerId,
                    targetMenuSlotId,
                    carriedIdentity,
                    cursorMode
            );
        }
    }

    enum CursorMode {
        ONE,
        STACK
    }

    record DistributeCursor(
            String expectedSessionFingerprint,
            int expectedContainerId,
            List<MenuSlotId> targetMenuSlotIds,
            ItemIdentity carriedIdentity,
            CursorMode cursorMode
    ) implements CraftingIntent {
        public DistributeCursor {
            expectedSessionFingerprint = expectedSessionFingerprint == null ? "" : expectedSessionFingerprint;
            targetMenuSlotIds = copyMenuSlotIds(targetMenuSlotIds);
            Objects.requireNonNull(cursorMode, "cursorMode");
        }

        public static DistributeCursor forCurrentSession(
                int containerId,
                List<MenuSlotId> targetMenuSlotIds,
                ItemIdentity carriedIdentity,
                CursorMode cursorMode
        ) {
            return new DistributeCursor(
                    ActionRequestClientContext.currentSessionFingerprint(containerId),
                    containerId,
                    targetMenuSlotIds,
                    carriedIdentity,
                    cursorMode
            );
        }

        private static List<MenuSlotId> copyMenuSlotIds(List<MenuSlotId> targetMenuSlotIds) {
            if (targetMenuSlotIds == null || targetMenuSlotIds.isEmpty()) {
                return List.of();
            }

            List<MenuSlotId> normalized = new ArrayList<>(targetMenuSlotIds.size());
            for (MenuSlotId targetMenuSlotId : targetMenuSlotIds) {
                normalized.add(targetMenuSlotId == null ? MenuSlotId.INVALID : targetMenuSlotId);
            }
            return List.copyOf(normalized);
        }
    }

    record ExtractResult(
            String expectedSessionFingerprint,
            int expectedContainerId,
            MenuSlotId resultMenuSlotId,
            ResultAction resultAction,
            int mouseButton,
            int repeatCount
    ) implements CraftingIntent {
        public ExtractResult {
            expectedSessionFingerprint = expectedSessionFingerprint == null ? "" : expectedSessionFingerprint;
            resultMenuSlotId = resultMenuSlotId == null ? MenuSlotId.of(-1) : resultMenuSlotId;
            Objects.requireNonNull(resultAction, "resultAction");
            mouseButton = mouseButton == 1 ? 1 : 0;
            repeatCount = Math.max(1, repeatCount);
        }

        public static ExtractResult forCurrentSession(
                int containerId,
                MenuSlotId resultMenuSlotId,
                ResultAction resultAction,
                int mouseButton,
                int repeatCount
        ) {
            return new ExtractResult(
                    ActionRequestClientContext.currentSessionFingerprint(containerId),
                    containerId,
                    resultMenuSlotId,
                    resultAction,
                    mouseButton,
                    repeatCount
            );
        }
    }

    enum ResultAction {
        PICKUP,
        QUICK_MOVE
    }
}
