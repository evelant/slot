package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;

import java.util.Locale;

public final class ContextualEventSignature {
    private ContextualEventSignature() {
    }

    public static boolean trainsAssociations(ContextualSignalEvent event) {
        if (event == null) {
            return false;
        }
        return switch (event.kind()) {
            case STATION_CONTENTS_CHANGED, ITEM_TAKEN_FROM_STORAGE -> true;
            case ITEM_DEPOSITED_TO_STORAGE, ITEM_PLACED, ITEM_CONSUMED, STATION_OPENED,
                    GOAL_CONTEXT_OBSERVED, RECIPE_CONTEXT_OBSERVED, ITEM_USED, ITEM_DAMAGED,
                    ITEM_ACQUIRED, ITEM_CRAFTED_OR_PRODUCED -> false;
        };
    }

    public static boolean trainsItemCandidate(ContextualSignalEvent event) {
        if (event == null || event.identity() == null) {
            return false;
        }
        return switch (event.kind()) {
            case ITEM_DEPOSITED_TO_STORAGE -> false;
            case STATION_OPENED, GOAL_CONTEXT_OBSERVED, RECIPE_CONTEXT_OBSERVED -> false;
            case STATION_CONTENTS_CHANGED, ITEM_ACQUIRED, ITEM_TAKEN_FROM_STORAGE,
                    ITEM_CRAFTED_OR_PRODUCED -> true;
            case ITEM_USED, ITEM_DAMAGED -> false;
            case ITEM_PLACED, ITEM_CONSUMED -> false;
        };
    }

    public static String key(ContextualSignalEvent event) {
        if (event == null || !trainsAssociations(event)) {
            return "";
        }
        String kind = event.kind().name().toLowerCase(Locale.ROOT);
        String context = clean(event.contextKey());
        String item = itemId(event.identity());
        String action = clean(event.metadataValue("action"));
        String target = clean(event.metadataValue("target"));
        String change = clean(event.metadataValue("change"));
        return switch (event.kind()) {
            case STATION_OPENED -> "station_opened|context=" + context;
            case STATION_CONTENTS_CHANGED ->
                    "station_contents|context=" + context + "|item=" + item + "|change=" + change;
            case ITEM_USED -> "item_used|item=" + item + "|action=" + action + "|target=" + target;
            case ITEM_PLACED -> "item_placed|item=" + item + "|target=" + target;
            case ITEM_CONSUMED -> "item_consumed|item=" + item;
            case ITEM_DAMAGED -> "item_damaged|item=" + item + "|action=" + action;
            case ITEM_ACQUIRED -> "item_acquired|item=" + item;
            case ITEM_TAKEN_FROM_STORAGE -> "item_taken|item=" + item;
            case ITEM_CRAFTED_OR_PRODUCED -> "item_produced|item=" + item;
            case GOAL_CONTEXT_OBSERVED -> "goal_context|context=" + context;
            case RECIPE_CONTEXT_OBSERVED -> "recipe_context|context=" + context;
            case ITEM_DEPOSITED_TO_STORAGE -> "";
        };
    }

    public static boolean replayableSignature(String signature) {
        String normalized = clean(signature);
        return normalized.startsWith("station_contents|")
                || normalized.startsWith("item_taken|");
    }

    private static String itemId(ItemIdentity identity) {
        return identity == null ? "" : clean(identity.itemId());
    }

    private static String clean(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
