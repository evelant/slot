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
}
