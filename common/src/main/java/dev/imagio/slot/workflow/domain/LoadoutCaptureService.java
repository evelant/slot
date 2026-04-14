package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.EquipmentGroupDescriptor;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.QuickAccessLaneDescriptor;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.inventory.query.InventorySourceSnapshot;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

public final class LoadoutCaptureService {
    private LoadoutCaptureService() {
    }

    public static Set<QuickAccessLoadoutEntry> captureEntries(
            InventoryAuthoritySnapshot authority,
            Function<InventoryEntrySnapshot, ItemIdentity> identityResolver
    ) {
        if (authority == null || authority.host() == null || identityResolver == null) {
            return Set.of();
        }

        LinkedHashSet<QuickAccessLoadoutEntry> entries = new LinkedHashSet<>();
        captureQuickAccessEntries(authority, identityResolver, entries);
        captureEquipmentEntries(authority, identityResolver, entries);
        return Set.copyOf(entries);
    }

    public static QuickAccessLoadoutDefinition captureDefinition(
            String id,
            String name,
            InventoryAuthoritySnapshot authority,
            Function<InventoryEntrySnapshot, ItemIdentity> identityResolver
    ) {
        return new QuickAccessLoadoutDefinition(
                id == null ? "" : id,
                name == null ? "" : name,
                captureEntries(authority, identityResolver)
        );
    }

    private static void captureQuickAccessEntries(
            InventoryAuthoritySnapshot authority,
            Function<InventoryEntrySnapshot, ItemIdentity> identityResolver,
            Set<QuickAccessLoadoutEntry> entries
    ) {
        for (QuickAccessLaneDescriptor lane : authority.host().quickAccessLanes()) {
            if (lane == null || lane.sourceId() == null || lane.sourceId().isBlank()) {
                continue;
            }
            InventorySourceSnapshot source = authority.sourceSnapshot(lane.sourceId());
            if (source == null) {
                continue;
            }
            for (InventoryEntrySnapshot snapshot : source.entries()) {
                QuickAccessLoadoutEntry entry = toQuickAccessEntry(lane, snapshot, identityResolver);
                if (entry != null) {
                    entries.add(entry);
                }
            }
        }
    }

    private static void captureEquipmentEntries(
            InventoryAuthoritySnapshot authority,
            Function<InventoryEntrySnapshot, ItemIdentity> identityResolver,
            Set<QuickAccessLoadoutEntry> entries
    ) {
        for (EquipmentGroupDescriptor group : authority.host().equipmentGroups()) {
            if (group == null || group.sourceId() == null || group.sourceId().isBlank()) {
                continue;
            }
            InventorySourceSnapshot source = authority.sourceSnapshot(group.sourceId());
            if (source == null) {
                continue;
            }
            for (InventoryEntrySnapshot snapshot : source.entries()) {
                QuickAccessLoadoutEntry entry = toEquipmentEntry(group, snapshot, identityResolver);
                if (entry != null) {
                    entries.add(entry);
                }
            }
        }
    }

    private static QuickAccessLoadoutEntry toQuickAccessEntry(
            QuickAccessLaneDescriptor lane,
            InventoryEntrySnapshot snapshot,
            Function<InventoryEntrySnapshot, ItemIdentity> identityResolver
    ) {
        if (lane == null || snapshot == null || !snapshot.slotBacked() || snapshot.stack() == null || snapshot.stack().isEmpty()) {
            return null;
        }
        ItemIdentity identity = identityResolver.apply(snapshot);
        if (identity == null) {
            return null;
        }
        return new QuickAccessLoadoutEntry(
                new LoadoutTarget.QuickAccessLaneTarget(lane.id(), snapshot.slotIndex()),
                identity
        );
    }

    private static QuickAccessLoadoutEntry toEquipmentEntry(
            EquipmentGroupDescriptor group,
            InventoryEntrySnapshot snapshot,
            Function<InventoryEntrySnapshot, ItemIdentity> identityResolver
    ) {
        if (group == null || snapshot == null || !snapshot.slotBacked() || snapshot.stack() == null || snapshot.stack().isEmpty()) {
            return null;
        }
        ItemIdentity identity = identityResolver.apply(snapshot);
        if (identity == null) {
            return null;
        }
        return new QuickAccessLoadoutEntry(
                new LoadoutTarget.EquipmentSlotTarget(group.id(), snapshot.slotIndex()),
                identity
        );
    }
}
