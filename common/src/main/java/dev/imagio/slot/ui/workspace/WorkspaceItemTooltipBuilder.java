package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.inventory.workspace.WorkspaceItemTargets;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class WorkspaceItemTooltipBuilder {
    private static final int MAX_CHEST_LABELS = 2;

    private WorkspaceItemTooltipBuilder() {
    }

    public static List<Component> slotLines(SlotWorkspaceViewModel.AtlasItem item) {
        return slotLines(item, null, false, false);
    }

    public static List<Component> slotLines(
            SlotWorkspaceViewModel.AtlasItem item,
            boolean hasProximateDepositRoute
    ) {
        return slotLines(item, null, false, hasProximateDepositRoute);
    }

    public static List<Component> slotLines(
            SlotWorkspaceViewModel.AtlasItem item,
            SlotWorkspaceViewModel.ContextualSuggestionLane suggestionLane,
            boolean includeContextualDebug
    ) {
        return slotLines(item, suggestionLane, includeContextualDebug, false);
    }

    public static List<Component> slotLines(
            SlotWorkspaceViewModel.AtlasItem item,
            SlotWorkspaceViewModel.ContextualSuggestionLane suggestionLane,
            boolean includeContextualDebug,
            boolean hasProximateDepositRoute
    ) {
        if (item == null) {
            return List.of();
        }
        ArrayList<Component> lines = new ArrayList<>();
        addDesiredLine(lines, item);
        addPutAwayLine(lines, item);
        addCarriedLine(lines, item);
        addStorageLine(lines, "Nearby pip", sum(item.presence()), item.presence());
        addProximateRouteLine(lines, item, hasProximateDepositRoute || item.putAwayState().routed());
        addStorageLine(lines, "Stored elsewhere", sum(item.elsewhere()), item.elsewhere());
        addMissingTargetLine(lines, item);
        addContainerLine(lines, item);
        if (includeContextualDebug) {
            addContextualDebugLines(lines, item, suggestionLane);
        }
        if (lines.isEmpty()) {
            return List.of();
        }
        ArrayList<Component> result = new ArrayList<>(lines.size() + 2);
        result.add(Component.empty());
        result.add(Component.literal("SLOT"));
        result.addAll(lines);
        return List.copyOf(result);
    }

    private static void addDesiredLine(ArrayList<Component> lines, SlotWorkspaceViewModel.AtlasItem item) {
        int desired = item.desiredCount();
        int carried = item.carried() ? item.totalCount() : 0;
        if (desired > 0) {
            String source = item.desiredCountFromKit() ? " tab" : "";
            lines.add(Component.literal("Desired badge: " + carried + "/" + desired + source));
            if (item.wantedCount() <= 0) {
                return;
            }
        }
        if (item.wantedCount() > 0) {
            lines.add(Component.literal("Wanted: " + carried + "/" + item.wantedCount()));
            return;
        }
        if (item.kitNeeded()) {
            lines.add(Component.literal("Tab marker: needed by active workflow tab"));
        }
    }

    private static void addCarriedLine(ArrayList<Component> lines, SlotWorkspaceViewModel.AtlasItem item) {
        if (!item.carried() || item.desiredCount() > 0 || item.wantedCount() > 0 || item.totalCount() <= 1) {
            return;
        }
        lines.add(Component.literal("Carried count: " + item.totalCount()));
    }

    private static void addPutAwayLine(ArrayList<Component> lines, SlotWorkspaceViewModel.AtlasItem item) {
        if (item.putAwayState().routed()) {
            lines.add(Component.literal("Put away: learned route nearby"));
        } else if (item.putAwayState().noRoute()) {
            lines.add(Component.literal("Put away: no learned home nearby"));
        }
    }

    private static void addStorageLine(
            ArrayList<Component> lines,
            String label,
            int count,
            List<SlotWorkspaceViewModel.ChestPresenceEntry> entries
    ) {
        if (count <= 0) {
            return;
        }
        StringBuilder text = new StringBuilder(label).append(": ").append(count);
        String breakdown = chestBreakdown(entries);
        if (!breakdown.isBlank()) {
            text.append(" in ").append(breakdown);
        }
        lines.add(Component.literal(text.toString()));
    }

    private static void addProximateRouteLine(
            ArrayList<Component> lines,
            SlotWorkspaceViewModel.AtlasItem item,
            boolean hasProximateDepositRoute
    ) {
        if (!hasProximateDepositRoute || sum(item.presence()) > 0) {
            return;
        }
        lines.add(Component.literal("Nearby pip: deposit route available"));
    }

    private static void addMissingTargetLine(ArrayList<Component> lines, SlotWorkspaceViewModel.AtlasItem item) {
        int carried = item.carried() ? item.totalCount() : 0;
        int stored = sum(item.presence()) + sum(item.elsewhere());
        int target = WorkspaceItemTargets.from(item).displayTargetCount();
        int missing = target - carried - stored;
        if (missing <= 0) {
            return;
        }
        lines.add(Component.literal("Need to craft/find: " + missing));
    }

    private static void addContainerLine(ArrayList<Component> lines, SlotWorkspaceViewModel.AtlasItem item) {
        if (!item.isCarriedContainer() || item.containerSlotCapacity() <= 0) {
            return;
        }
        lines.add(Component.literal(
                "Container: " + item.containerFreeSlotCount() + "/" + item.containerSlotCapacity() + " slots free"));
    }

    private static void addContextualDebugLines(
            ArrayList<Component> lines,
            SlotWorkspaceViewModel.AtlasItem item,
            SlotWorkspaceViewModel.ContextualSuggestionLane suggestionLane
    ) {
        if (suggestionLane == null) {
            return;
        }
        SlotWorkspaceViewModel.ContextualSuggestionDebugInfo info = suggestionLane.debugInfoFor(item);
        if (info == null) {
            return;
        }
        String label = suggestionLane.label().isBlank() ? "Suggestion" : suggestionLane.label();
        lines.add(Component.literal("Contextual score (" + label + ")"));
        for (String reason : info.reasons()) {
            lines.add(Component.literal("  " + reason));
        }
    }

    private static String chestBreakdown(List<SlotWorkspaceViewModel.ChestPresenceEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return "";
        }
        ArrayList<String> parts = new ArrayList<>();
        int positiveEntries = 0;
        for (SlotWorkspaceViewModel.ChestPresenceEntry entry : entries) {
            if (entry == null || entry.count() <= 0) {
                continue;
            }
            positiveEntries++;
            if (parts.size() >= MAX_CHEST_LABELS) {
                continue;
            }
            String label = entry.label().isBlank() ? "chest" : entry.label();
            parts.add(label + ": " + entry.count());
        }
        if (positiveEntries > MAX_CHEST_LABELS) {
            parts.add("+" + (positiveEntries - MAX_CHEST_LABELS) + " more");
        }
        return String.join(", ", parts);
    }

    private static int sum(List<SlotWorkspaceViewModel.ChestPresenceEntry> entries) {
        int total = 0;
        if (entries == null) {
            return 0;
        }
        for (SlotWorkspaceViewModel.ChestPresenceEntry entry : entries) {
            if (entry != null) {
                total += entry.count();
            }
        }
        return total;
    }

}
