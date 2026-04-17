package dev.imagio.slot.inventory.triage;

import dev.imagio.slot.inventory.core.ItemIdentity;

public record ChipSuggestion(
        ChipKind kind,
        IslandSuggestionTemplate template,
        String islandId,
        String label,
        int color,
        ItemIdentity iconIdentity
) {
    public ChipSuggestion {
        if (kind == ChipKind.TEMPLATE && template == null) {
            throw new IllegalArgumentException("template chip requires a template");
        }
        if (kind == ChipKind.LEARNED && (islandId == null || islandId.isBlank())) {
            throw new IllegalArgumentException("learned chip requires an islandId");
        }
        label = label == null ? "" : label;
        islandId = islandId == null ? "" : islandId;
    }

    public static ChipSuggestion learned(String islandId, String label, int color, ItemIdentity iconIdentity) {
        return new ChipSuggestion(ChipKind.LEARNED, null, islandId, label, color, iconIdentity);
    }

    public static ChipSuggestion template(IslandSuggestionTemplate template, ItemIdentity iconIdentity) {
        return new ChipSuggestion(
                ChipKind.TEMPLATE,
                template,
                template.defaultIslandId(),
                template.defaultLabel(),
                template.defaultColor(),
                iconIdentity
        );
    }

    public enum ChipKind {
        LEARNED,
        TEMPLATE
    }
}
