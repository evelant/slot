package dev.imagio.slot.client.category;

import dev.imagio.slot.client.model.ItemIdentity;

import java.util.EnumSet;

public record CategorySubject(ItemIdentity identity, EnumSet<CategorySignal> signals) {
    public CategorySubject {
        signals = signals.isEmpty() ? EnumSet.noneOf(CategorySignal.class) : EnumSet.copyOf(signals);
    }

    public static CategorySubject of(ItemIdentity identity, CategorySignal... signals) {
        EnumSet<CategorySignal> values = EnumSet.noneOf(CategorySignal.class);
        java.util.Collections.addAll(values, signals);
        return new CategorySubject(identity, values);
    }
}
