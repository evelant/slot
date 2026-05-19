package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Transitional storage shape for a player-authored workflow tab: name,
 * optional parent tab, hotbar pages, optional offhand pin, and membership.
 * The "bring" list (non-hotbar identities the kit wanted in carry) was
 * folded into kit-scoped desired counts — see
 * {@link DesiredCountWorkflowDomainService#forKit(String)}. Kit
 * activation auto-fetches toward those counts in lieu of the old bring
 * fetch path.
 */
public record KitDefinition(
        String id,
        String name,
        List<KitPage> pages,
        ItemIdentity offhand,
        String parentId,
        Set<ItemIdentity> members,
        Set<WorkflowAcceptedInputRule> acceptedInputs
) {
    public static final int MAX_CARRIED_CAPACITY = 36;

    public KitDefinition(String id, String name, List<KitPage> pages, ItemIdentity offhand) {
        this(id, name, pages, offhand, "", Set.of(), Set.of());
    }

    public KitDefinition(
            String id,
            String name,
            List<KitPage> pages,
            ItemIdentity offhand,
            String parentId,
            Set<ItemIdentity> members
    ) {
        this(id, name, pages, offhand, parentId, members, Set.of());
    }

    public KitDefinition {
        id = id == null ? "" : id;
        name = name == null ? "" : name;
        pages = pages == null || pages.isEmpty()
                ? List.of(KitPage.empty())
                : List.copyOf(pages);
        parentId = parentId == null ? "" : parentId.trim();
        members = members == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(members));
        acceptedInputs = acceptedInputs == null
                ? Set.of()
                : Set.copyOf(new LinkedHashSet<>(acceptedInputs));
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

    /**
     * Hotbar slots claimed by the kit. Kit-scoped desired counts can add
     * non-hotbar identities the player wants alongside, but those don't
     * consume a kit slot — they live in carry wherever there's room.
     */
    public int carriedSlotCount() {
        return pages.size() * KitPage.HOTBAR_SLOT_COUNT;
    }

    public boolean fitsCarriedCapacity() {
        return carriedSlotCount() <= MAX_CARRIED_CAPACITY;
    }

    public boolean variant() {
        return !parentId.isBlank();
    }

    public KitDefinition withName(String nextName) {
        return new KitDefinition(id, nextName, pages, offhand, parentId, members, acceptedInputs);
    }

    public KitDefinition withPages(List<KitPage> nextPages) {
        return new KitDefinition(id, name, nextPages, offhand, parentId, members, acceptedInputs);
    }

    public KitDefinition withOffhand(ItemIdentity nextOffhand) {
        return new KitDefinition(id, name, pages, nextOffhand, parentId, members, acceptedInputs);
    }

    public KitDefinition withParentId(String nextParentId) {
        return new KitDefinition(id, name, pages, offhand, nextParentId, members, acceptedInputs);
    }

    public KitDefinition withMembers(Set<ItemIdentity> nextMembers) {
        return new KitDefinition(id, name, pages, offhand, parentId, nextMembers, acceptedInputs);
    }

    public KitDefinition withAcceptedInputs(Set<WorkflowAcceptedInputRule> nextAcceptedInputs) {
        return new KitDefinition(id, name, pages, offhand, parentId, members, nextAcceptedInputs);
    }

    public KitDefinition withMember(ItemIdentity identity) {
        if (identity == null || members.contains(identity)) {
            return this;
        }
        LinkedHashSet<ItemIdentity> next = new LinkedHashSet<>(members);
        next.add(identity);
        return withMembers(next);
    }

    public KitDefinition withoutMember(ItemIdentity identity) {
        if (identity == null || !members.contains(identity)) {
            return this;
        }
        LinkedHashSet<ItemIdentity> next = new LinkedHashSet<>(members);
        next.remove(identity);
        return withMembers(next);
    }

    public KitDefinition withAcceptedInput(WorkflowAcceptedInputRule rule) {
        if (rule == null || acceptedInputs.contains(rule)) {
            return this;
        }
        LinkedHashSet<WorkflowAcceptedInputRule> next = new LinkedHashSet<>(acceptedInputs);
        next.add(rule);
        return withAcceptedInputs(next);
    }

    public KitDefinition withoutAcceptedInput(WorkflowAcceptedInputRule rule) {
        if (rule == null || !acceptedInputs.contains(rule)) {
            return this;
        }
        LinkedHashSet<WorkflowAcceptedInputRule> next = new LinkedHashSet<>(acceptedInputs);
        next.remove(rule);
        return withAcceptedInputs(next);
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
