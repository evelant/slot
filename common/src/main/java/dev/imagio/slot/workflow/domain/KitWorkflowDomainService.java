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
        return create(name, KitPage.empty(), List.of(), null, DomainEventMetadata.origin("workflow.kit.create"));
    }

    public KitDefinition create(
            String name,
            KitPage firstPage,
            List<ItemIdentity> bring,
            ItemIdentity offhand,
            DomainEventMetadata metadata
    ) {
        String normalizedName = normalizeName(name);
        String id = uniqueSlug(normalizedName, existingIds());
        KitDefinition kit = new KitDefinition(
                id,
                normalizedName,
                List.of(firstPage == null ? KitPage.empty() : firstPage),
                bring,
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
        repository.appendWorkflowEvent(
                new WorkflowEvent.KitUpdated(next),
                resolveMetadata(metadata, "workflow.kit.update")
        );
        notifyMutated();
        return true;
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
        KitPage page = capturePageFromAuthority(authority, identityResolver);
        ItemIdentity offhand = captureOffhandIdentity(authority, identityResolver);
        KitDefinition kit = new KitDefinition(id, normalizedName, List.of(page), List.of(), offhand);
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
        return LoadoutApplyService.plan(
                loadout,
                authority,
                protectionPolicy,
                InventoryActionMode.EXECUTE,
                identityResolver == null
                        ? entry -> ItemIdentityMatcher.create(entry.stack())
                        : identityResolver
        );
    }

    private KitPage capturePageFromAuthority(
            InventoryAuthoritySnapshot authority,
            Function<InventoryEntrySnapshot, ItemIdentity> identityResolver
    ) {
        if (authority == null || identityResolver == null) {
            return KitPage.empty();
        }
        List<ItemIdentity> slots = new ArrayList<>();
        for (int index = 0; index < KitPage.HOTBAR_SLOT_COUNT; index++) {
            slots.add(null);
        }
        for (InventoryEntrySnapshot entry : authority.entries(BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0)) {
            int slotIndex = entry.slotIndex();
            if (slotIndex < 0 || slotIndex >= KitPage.HOTBAR_SLOT_COUNT) {
                continue;
            }
            if (!entry.present()) {
                continue;
            }
            ItemIdentity identity = identityResolver.apply(entry);
            if (identity != null) {
                slots.set(slotIndex, identity);
            }
        }
        return new KitPage(slots);
    }

    private ItemIdentity captureOffhandIdentity(
            InventoryAuthoritySnapshot authority,
            Function<InventoryEntrySnapshot, ItemIdentity> identityResolver
    ) {
        if (authority == null || identityResolver == null) {
            return null;
        }
        for (InventoryEntrySnapshot entry : authority.entries(BuiltinInventoryIds.PLAYER_OFFHAND)) {
            if (!entry.present()) {
                continue;
            }
            ItemIdentity identity = identityResolver.apply(entry);
            if (identity != null) {
                return identity;
            }
        }
        return null;
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
