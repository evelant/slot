package dev.imagio.slot.client.screen;

import dev.imagio.slot.projection.InventoryViewData;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public final class InventoryRailSupport {
    private InventoryRailSupport() {
    }

    public static List<Target> buildTargets(
            String allTargetId,
            String allLabel,
            List<InventoryViewData.Section> sections
    ) {
        List<Target> targets = new ArrayList<>();
        targets.add(new Target(allTargetId, allLabel, Kind.ALL));
        if (sections != null) {
            for (InventoryViewData.Section section : sections) {
                targets.add(new Target(section.id(), section.label(), kindFor(section)));
            }
        }
        return List.copyOf(targets);
    }

    public static String resolveVisibleTargetId(
            String allTargetId,
            List<Target> targets,
            String requestedId,
            Predicate<String> accessible
    ) {
        if (allTargetId.equals(requestedId) || accessible.test(requestedId)) {
            return requestedId;
        }

        int targetIndex = -1;
        for (int index = 0; index < targets.size(); index++) {
            if (targets.get(index).id().equals(requestedId)) {
                targetIndex = index;
                break;
            }
        }

        if (targetIndex < 0) {
            return requestedId;
        }

        for (int index = targetIndex + 1; index < targets.size(); index++) {
            Target candidate = targets.get(index);
            if (candidate.kind() != Kind.ALL && accessible.test(candidate.id())) {
                return candidate.id();
            }
        }

        for (int index = targetIndex - 1; index >= 0; index--) {
            Target candidate = targets.get(index);
            if (candidate.kind() != Kind.ALL && accessible.test(candidate.id())) {
                return candidate.id();
            }
        }

        return allTargetId;
    }

    public static int contentHeight(List<Target> targets, int rowHeight, int groupHeaderHeight) {
        int height = 0;
        Kind previousKind = null;
        for (Target target : targets) {
            if (target.kind() != previousKind && target.kind() != Kind.ALL) {
                height += groupHeaderHeight;
            }
            height += rowHeight;
            previousKind = target.kind();
        }
        return height;
    }

    public static Target hitTarget(
            List<Target> targets,
            double mouseX,
            double mouseY,
            int rowLeft,
            int rowRight,
            int contentTop,
            int scrollOffset,
            int rowHeight,
            int groupHeaderHeight
    ) {
        int y = contentTop - scrollOffset;
        Kind previousKind = null;
        for (Target target : targets) {
            if (target.kind() != previousKind && target.kind() != Kind.ALL) {
                y += groupHeaderHeight;
                previousKind = target.kind();
            }

            int top = y - 2;
            int bottom = y + rowHeight - 2;
            if (mouseX >= rowLeft && mouseX <= rowRight && mouseY >= top && mouseY <= bottom) {
                return target;
            }

            y += rowHeight;
            previousKind = target.kind();
        }
        return null;
    }

    private static Kind kindFor(InventoryViewData.Section section) {
        if (section.isRecent()) {
            return Kind.RECENT;
        }
        if (section.isCollection()) {
            return Kind.COLLECTION;
        }
        if (section.isModBucket()) {
            return Kind.MOD_BUCKET;
        }
        return Kind.CATEGORY;
    }

    public record Target(String id, String label, Kind kind) {
    }

    public enum Kind {
        ALL,
        RECENT,
        COLLECTION,
        MOD_BUCKET,
        CATEGORY
    }
}
