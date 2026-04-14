package dev.imagio.slot.client.screen.debug;

import net.minecraft.network.chat.Component;

import java.util.Comparator;
import dev.imagio.slot.projection.InventoryViewData;

public enum SlotDebugSortMode {
    NAME("slot.screen.debug.sort.name", "slot.screen.debug.sort.short.name"),
    COUNT("slot.screen.debug.sort.count", "slot.screen.debug.sort.short.count"),
    CATEGORY("slot.screen.debug.sort.category", "slot.screen.debug.sort.short.category");

    private final String translationKey;
    private final String shortTranslationKey;

    SlotDebugSortMode(String translationKey, String shortTranslationKey) {
        this.translationKey = translationKey;
        this.shortTranslationKey = shortTranslationKey;
    }

    public SlotDebugSortMode next() {
        SlotDebugSortMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public Component displayName() {
        return Component.translatable(translationKey);
    }

    public Component shortLabel() {
        return Component.translatable(shortTranslationKey);
    }

    public Comparator<InventoryViewData.EntryView> comparator() {
        return switch (this) {
            case NAME -> Comparator
                    .comparing(InventoryViewData.EntryView::displayName, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(Comparator.comparingInt((InventoryViewData.EntryView view) -> view.itemEntry().totalCount()).reversed())
                    .thenComparing(view -> view.itemEntry().identity().itemId());
            case COUNT -> Comparator
                    .comparingInt((InventoryViewData.EntryView view) -> view.itemEntry().totalCount())
                    .reversed()
                    .thenComparing(InventoryViewData.EntryView::displayName, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(view -> view.itemEntry().identity().itemId());
            case CATEGORY -> Comparator
                    .comparing((InventoryViewData.EntryView view) -> view.itemEntry().category())
                    .thenComparing(InventoryViewData.EntryView::displayName, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(Comparator.comparingInt((InventoryViewData.EntryView view) -> view.itemEntry().totalCount()).reversed())
                    .thenComparing(view -> view.itemEntry().identity().itemId());
        };
    }
}