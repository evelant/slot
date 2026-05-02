package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.action.InventoryActionMode;
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

public final class KitWorkflowDomainService {
    private final WorkflowDomainStateRepository repository;
    private final Runnable mutationObserver;

    public KitWorkflowDomainService(WorkflowDomainStateRepository repository) {
        this(repository, () -> {
        });
    }

    public KitWorkflowDomainService(WorkflowDomainStateRepository repository, Runnable mutationObserver) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.mutationObserver = mutationObserver == null ? () -> {
        } : mutationObserver;
    }

    public KitMap kitMap() {
        return repository.workflowProjection().kitMap();
    }

    public List<KitDefinition> kits() {
        return kitMap().kits();
    }

    public KitActivation activation() {
        return kitMap().activation();
    }

    public KitDefinition kit(String kitId) {
        return kitMap().kit(kitId);
    }

    public KitDefinition create(String name) {
        return create(name, KitPage.empty(), null, DomainEventMetadata.origin("workflow.kit.create"));
    }

    public KitDefinition create(
            String name,
            KitPage firstPage,
            ItemIdentity offhand,
            DomainEventMetadata metadata
    ) {
        String normalizedName = normalizeName(name);
        String id = uniqueSlug(normalizedName, existingIds());
        KitDefinition kit = new KitDefinition(
                id,
                normalizedName,
                List.of(firstPage == null ? KitPage.empty() : firstPage),
                offhand
        );
        repository.appendWorkflowEvent(
                new WorkflowEvent.KitCreated(kit),
                resolveMetadata(metadata, "workflow.kit.create")
        );
        notifyMutated();
        return kit(id);
    }

    public boolean rename(String kitId, String newName) {
        return rename(kitId, newName, DomainEventMetadata.origin("workflow.kit.rename"));
    }

    public boolean rename(String kitId, String newName, DomainEventMetadata metadata) {
        KitDefinition existing = requireKit(kitId);
        String normalizedName = normalizeName(newName);
        if (existing.name().equals(normalizedName)) {
            return false;
        }
        repository.appendWorkflowEvent(
                new WorkflowEvent.KitUpdated(existing.withName(normalizedName)),
                resolveMetadata(metadata, "workflow.kit.rename")
        );
        notifyMutated();
        return true;
    }

    public boolean update(KitDefinition next) {
        return update(next, DomainEventMetadata.origin("workflow.kit.update"));
    }

    public boolean update(KitDefinition next, DomainEventMetadata metadata) {
        if (next == null || next.id().isBlank()) {
            return false;
        }
        KitDefinition existing = requireKit(next.id());
        if (existing.equals(next)) {
            return false;
        }
        if (!next.fitsCarriedCapacity()) {
            throw new IllegalArgumentException(
                    "Kit exceeds carried capacity: " + next.carriedSlotCount()
                            + " > " + KitDefinition.MAX_CARRIED_CAPACITY);
        }
        repository.appendWorkflowEvent(
                new WorkflowEvent.KitUpdated(next),
                resolveMetadata(metadata, "workflow.kit.update")
        );
        notifyMutated();
        return true;
    }

    public boolean addPage(String kitId) {
        return addPage(kitId, DomainEventMetadata.origin("workflow.kit.add_page"));
    }

    public boolean addPage(String kitId, DomainEventMetadata metadata) {
        KitDefinition existing = requireKit(kitId);
        KitDefinition next = existing.withPageAppended(KitPage.empty());
        if (!next.fitsCarriedCapacity()) {
            throw new IllegalArgumentException(
                    "Adding a page would exceed carried capacity: " + next.carriedSlotCount()
                            + " > " + KitDefinition.MAX_CARRIED_CAPACITY);
        }
        repository.appendWorkflowEvent(
                new WorkflowEvent.KitUpdated(next),
                resolveMetadata(metadata, "workflow.kit.add_page")
        );
        notifyMutated();
        return true;
    }

    public boolean removePage(String kitId, int pageIndex) {
        return removePage(kitId, pageIndex, DomainEventMetadata.origin("workflow.kit.remove_page"));
    }

    public boolean setSlotIdentity(String kitId, int pageIndex, int slotIndex, ItemIdentity identity) {
        return setSlotIdentity(kitId, pageIndex, slotIndex, identity,
                DomainEventMetadata.origin("workflow.kit.set_slot"));
    }

    public boolean setSlotIdentity(String kitId, int pageIndex, int slotIndex, ItemIdentity identity, DomainEventMetadata metadata) {
        KitDefinition existing = requireKit(kitId);
        KitPage page = existing.page(pageIndex);
        if (page == null || slotIndex < 0 || slotIndex >= KitPage.HOTBAR_SLOT_COUNT) {
            return false;
        }
        ItemIdentity currentIdentity = page.slot(slotIndex);
        if (Objects.equals(currentIdentity, identity)) {
            return false;
        }
        KitPage nextPage = page.withSlot(slotIndex, identity);
        repository.appendWorkflowEvent(
                new WorkflowEvent.KitUpdated(existing.withPageReplaced(pageIndex, nextPage)),
                resolveMetadata(metadata, "workflow.kit.set_slot")
        );
        notifyMutated();
        return true;
    }

    public boolean swapSlots(String kitId, int pageIndex, int fromIndex, int toIndex) {
        return swapSlots(kitId, pageIndex, fromIndex, toIndex,
                DomainEventMetadata.origin("workflow.kit.swap_slots"));
    }

    public boolean swapSlots(String kitId, int pageIndex, int fromIndex, int toIndex, DomainEventMetadata metadata) {
        if (fromIndex == toIndex) {
            return false;
        }
        KitDefinition existing = requireKit(kitId);
        KitPage page = existing.page(pageIndex);
        if (page == null
                || fromIndex < 0 || fromIndex >= KitPage.HOTBAR_SLOT_COUNT
                || toIndex < 0 || toIndex >= KitPage.HOTBAR_SLOT_COUNT) {
            return false;
        }
        ItemIdentity fromIdentity = page.slot(fromIndex);
        ItemIdentity toIdentity = page.slot(toIndex);
        if (Objects.equals(fromIdentity, toIdentity)) {
            return false;
        }
        KitPage nextPage = page.withSlot(fromIndex, toIdentity).withSlot(toIndex, fromIdentity);
        repository.appendWorkflowEvent(
                new WorkflowEvent.KitUpdated(existing.withPageReplaced(pageIndex, nextPage)),
                resolveMetadata(metadata, "workflow.kit.swap_slots")
        );
        notifyMutated();
        return true;
    }

    public boolean removePage(String kitId, int pageIndex, DomainEventMetadata metadata) {
        KitDefinition existing = requireKit(kitId);
        if (existing.pageCount() <= 1) {
            return false;
        }
        if (pageIndex < 0 || pageIndex >= existing.pageCount()) {
            return false;
        }
        KitDefinition next = existing.withPageRemoved(pageIndex);
        repository.appendWorkflowEvent(
                new WorkflowEvent.KitUpdated(next),
                resolveMetadata(metadata, "workflow.kit.remove_page")
        );
        // if the active page was at or beyond the removed index, slide activation back to a valid index
        KitActivation current = activation();
        if (current.isActive() && current.kitId().equals(kitId)) {
            int currentPage = current.pageIndex();
            int newPage = currentPage;
            if (currentPage == pageIndex) {
                newPage = Math.max(0, pageIndex - 1);
            } else if (currentPage > pageIndex) {
                newPage = currentPage - 1;
            }
            if (newPage != currentPage) {
                repository.appendWorkflowEvent(
                        new WorkflowEvent.KitPageSwitched(newPage),
                        resolveMetadata(metadata, "workflow.kit.remove_page.reindex")
                );
            }
        }
        notifyMutated();
        return true;
    }

    public KitDefinition duplicate(String kitId) {
        return duplicate(kitId, DomainEventMetadata.origin("workflow.kit.duplicate"));
    }

    public KitDefinition duplicate(String kitId, DomainEventMetadata metadata) {
        KitDefinition source = requireKit(kitId);
        String baseName = source.name() + " (copy)";
        String newId = uniqueSlug(baseName, existingIds());
        KitDefinition copy = new KitDefinition(
                newId,
                baseName,
                source.pages(),
                source.offhand()
        );
        repository.appendWorkflowEvent(
                new WorkflowEvent.KitCreated(copy),
                resolveMetadata(metadata, "workflow.kit.duplicate")
        );
        notifyMutated();
        return kit(newId);
    }

    public boolean delete(String kitId) {
        return delete(kitId, DomainEventMetadata.origin("workflow.kit.delete"));
    }

    public boolean delete(String kitId, DomainEventMetadata metadata) {
        requireKit(kitId);
        repository.appendWorkflowEvent(
                new WorkflowEvent.KitDeleted(kitId),
                resolveMetadata(metadata, "workflow.kit.delete")
        );
        notifyMutated();
        return true;
    }

    public KitDefinition snapshotFromAuthority(
            String name,
            InventoryAuthoritySnapshot authority,
            Function<InventoryEntrySnapshot, ItemIdentity> identityResolver
    ) {
        return snapshotFromAuthority(
                name,
                authority,
                identityResolver,
                DomainEventMetadata.origin("workflow.kit.snapshot")
        );
    }

    public KitDefinition snapshotFromAuthority(
            String name,
            InventoryAuthoritySnapshot authority,
            Function<InventoryEntrySnapshot, ItemIdentity> identityResolver,
            DomainEventMetadata metadata
    ) {
        String normalizedName = normalizeName(name);
        String id = uniqueSlug(normalizedName, existingIds());
        KitPage page = KitSnapshotSupport.capturePageFromAuthority(authority, identityResolver);
        ItemIdentity offhand = KitSnapshotSupport.captureOffhandIdentity(authority, identityResolver);
        KitDefinition kit = new KitDefinition(id, normalizedName, List.of(page), offhand);
        repository.appendWorkflowEvent(
                new WorkflowEvent.KitCreated(kit),
                resolveMetadata(metadata, "workflow.kit.snapshot")
        );
        notifyMutated();
        return kit(id);
    }

    public boolean activate(String kitId) {
        return activate(kitId, 0, DomainEventMetadata.origin("workflow.kit.activate"));
    }

    public boolean activate(String kitId, int pageIndex, DomainEventMetadata metadata) {
        KitDefinition existing = requireKit(kitId);
        int boundedPageIndex = Math.max(0, Math.min(pageIndex, existing.pageCount() - 1));
        KitActivation current = activation();
        if (current.kitId().equals(kitId) && current.pageIndex() == boundedPageIndex) {
            return false;
        }
        repository.appendWorkflowEvent(
                new WorkflowEvent.KitActivated(kitId, boundedPageIndex),
                resolveMetadata(metadata, "workflow.kit.activate")
        );
        notifyMutated();
        return true;
    }

    public boolean deactivate() {
        return deactivate(DomainEventMetadata.origin("workflow.kit.deactivate"));
    }

    public boolean deactivate(DomainEventMetadata metadata) {
        if (!activation().isActive()) {
            return false;
        }
        repository.appendWorkflowEvent(
                new WorkflowEvent.KitDeactivated(),
                resolveMetadata(metadata, "workflow.kit.deactivate")
        );
        notifyMutated();
        return true;
    }

    public boolean switchPage(int pageIndex) {
        return switchPage(pageIndex, DomainEventMetadata.origin("workflow.kit.switch_page"));
    }

    public boolean switchPage(int pageIndex, DomainEventMetadata metadata) {
        KitActivation current = activation();
        if (!current.isActive()) {
            return false;
        }
        KitDefinition active = requireKit(current.kitId());
        int bounded = Math.floorMod(pageIndex, Math.max(1, active.pageCount()));
        if (bounded == current.pageIndex()) {
            return false;
        }
        repository.appendWorkflowEvent(
                new WorkflowEvent.KitPageSwitched(bounded),
                resolveMetadata(metadata, "workflow.kit.switch_page")
        );
        notifyMutated();
        return true;
    }

    public QuickAccessLoadoutDefinition pageAsLoadout(KitDefinition kit, int pageIndex) {
        if (kit == null) {
            return null;
        }
        KitPage page = kit.page(pageIndex);
        if (page == null) {
            return null;
        }
        LinkedHashSet<QuickAccessLoadoutEntry> entries = new LinkedHashSet<>();
        for (int slotIndex = 0; slotIndex < KitPage.HOTBAR_SLOT_COUNT; slotIndex++) {
            ItemIdentity identity = page.slot(slotIndex);
            if (identity != null) {
                entries.add(new QuickAccessLoadoutEntry(
                        new LoadoutTarget.QuickAccessLaneTarget(BuiltinInventoryIds.QUICK_ACCESS_LANE_0, slotIndex),
                        identity
                ));
            }
        }
        String loadoutId = kit.id() + "#p" + pageIndex;
        String loadoutName = kit.name() + " (page " + (pageIndex + 1) + ")";
        return new QuickAccessLoadoutDefinition(loadoutId, loadoutName, entries);
    }

    public LoadoutApplyService.LoadoutApplyPlan planActivate(
            String kitId,
            int pageIndex,
            InventoryAuthoritySnapshot authority,
            ProtectionPolicy protectionPolicy,
            Function<InventoryEntrySnapshot, ItemIdentity> identityResolver
    ) {
        KitDefinition kit = kit(kitId);
        if (kit == null) {
            return LoadoutApplyService.LoadoutApplyPlan.empty("");
        }
        QuickAccessLoadoutDefinition loadout = pageAsLoadout(kit, pageIndex);
        if (loadout == null) {
            return LoadoutApplyService.LoadoutApplyPlan.empty("");
        }
        // Page slots with a null identity encode "this belt slot should be empty" —
        // pass them as clearTargets so any existing occupant gets staged out to main
        // rather than lingering on the belt under the new layout.
        KitPage page = kit.page(pageIndex);
        LinkedHashSet<LoadoutTarget> clearTargets = new LinkedHashSet<>();
        if (page != null) {
            for (int slotIndex = 0; slotIndex < KitPage.HOTBAR_SLOT_COUNT; slotIndex++) {
                if (page.slot(slotIndex) == null) {
                    clearTargets.add(new LoadoutTarget.QuickAccessLaneTarget(
                            BuiltinInventoryIds.QUICK_ACCESS_LANE_0, slotIndex));
                }
            }
        }
        return LoadoutApplyService.plan(
                loadout,
                clearTargets,
                authority,
                protectionPolicy,
                InventoryActionMode.EXECUTE,
                identityResolver == null
                        ? entry -> ItemIdentityMatcher.create(entry.stack())
                        : identityResolver
        );
    }

    private Set<String> existingIds() {
        LinkedHashSet<String> ids = new LinkedHashSet<>();
        for (KitDefinition kit : kits()) {
            ids.add(kit.id());
        }
        return ids;
    }

    private KitDefinition requireKit(String kitId) {
        KitDefinition kit = kit(kitId);
        if (kit == null) {
            throw new IllegalArgumentException("Unknown kit: " + kitId);
        }
        return kit;
    }

    private void notifyMutated() {
        mutationObserver.run();
    }

    private static String normalizeName(String name) {
        String normalized = name == null ? "" : name.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Kit name must not be blank");
        }
        return normalized;
    }

    private static DomainEventMetadata resolveMetadata(DomainEventMetadata metadata, String origin) {
        return (metadata == null ? DomainEventMetadata.origin("") : metadata).withOrigin(origin);
    }

    private static String uniqueSlug(String value, Set<String> existingIds) {
        String baseId = slugify(value);
        String candidate = baseId;
        int counter = 2;
        while (existingIds.contains(candidate)) {
            candidate = baseId + "-" + counter++;
        }
        return candidate;
    }

    private static String slugify(String value) {
        String normalized = value == null ? "" : value.toLowerCase(Locale.ROOT).trim().replaceAll("[^a-z0-9]+", "-");
        normalized = normalized.replaceAll("^-+", "").replaceAll("-+$", "");
        return normalized.isEmpty() ? "kit" : normalized;
    }
}
