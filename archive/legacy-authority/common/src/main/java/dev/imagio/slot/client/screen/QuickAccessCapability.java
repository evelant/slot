package dev.imagio.slot.client.screen;

import dev.imagio.slot.client.model.ItemIdentity;

import java.util.Set;

public interface QuickAccessCapability {
    boolean canAssignToQuickAccess(ItemIdentity identity);

    boolean canAssignToQuickAccess(ItemIdentity identity, Set<String> preferredSourceIds);

    boolean canUseFromInventory(ItemIdentity identity);

    boolean canUseFromInventory(ItemIdentity identity, Set<String> preferredSourceIds);

    boolean canDropFromInventory(ItemIdentity identity);

    boolean canDropFromInventory(ItemIdentity identity, Set<String> preferredSourceIds);
}
