package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;

import java.util.ArrayList;
import java.util.List;

public record KitDefinition(
        String id,
        String name,
        List<KitPage> pages,
        List<ItemIdentity> bring,
        ItemIdentity offhand
) {
    public static final int MAX_CARRIED_CAPACITY = 36;

    public KitDefinition {
        id = id == null ? "" : id;
        name = name == null ? "" : name;
        pages = pages == null || pages.isEmpty()
                ? List.of(KitPage.empty())
                : List.copyOf(pages);
        bring = bring == null ? List.of() : List.copyOf(bring);
    }

    public int pageCount() {
        return pages.size();
    }

    public KitPage page(int index) {
        if (index < 0 || index >= pages.size()) {
            return null;
        }
        return pages.get(index);
    }

    public int carriedSlotCount() {
        return pages.size() * KitPage.HOTBAR_SLOT_COUNT + bring.size();
    }

    public boolean fitsCarriedCapacity() {
        return carriedSlotCount() <= MAX_CARRIED_CAPACITY;
    }

    public KitDefinition withName(String nextName) {
        return new KitDefinition(id, nextName, pages, bring, offhand);
    }

    public KitDefinition withPages(List<KitPage> nextPages) {
        return new KitDefinition(id, name, nextPages, bring, offhand);
    }

    public KitDefinition withBring(List<ItemIdentity> nextBring) {
        return new KitDefinition(id, name, pages, nextBring, offhand);
    }

    public KitDefinition withOffhand(ItemIdentity nextOffhand) {
        return new KitDefinition(id, name, pages, bring, nextOffhand);
    }

    public KitDefinition withPageReplaced(int index, KitPage page) {
        if (index < 0 || index >= pages.size() || page == null) {
            return this;
        }
        ArrayList<KitPage> next = new ArrayList<>(pages);
        next.set(index, page);
        return withPages(next);
    }

    public KitDefinition withPageAppended(KitPage page) {
        ArrayList<KitPage> next = new ArrayList<>(pages);
        next.add(page == null ? KitPage.empty() : page);
        return withPages(next);
    }

    public KitDefinition withPageRemoved(int index) {
        if (index < 0 || index >= pages.size() || pages.size() <= 1) {
            return this;
        }
        ArrayList<KitPage> next = new ArrayList<>(pages);
        next.remove(index);
        return withPages(next);
    }
}
