package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Pure helpers for reading the current authority into Kit shape. Lives outside
 * {@link KitWorkflowDomainService} so UI callers can do partial updates (e.g.
 * "overwrite active page with current belt") without re-creating a whole Kit.
 */
public final class KitSnapshotSupport {
    private KitSnapshotSupport() {
    }

    public static KitPage capturePageFromAuthority(
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

    public static ItemIdentity captureOffhandIdentity(
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
}
