package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.ItemIdentity;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

/**
 * Shared workspace search semantics. UI backends may own input focus and modal
 * behavior, but query cleanup and item matching must stay identical.
 */
public final class WorkspaceSearchQuery {
    public static final int MAX_QUERY_LENGTH = 64;

    private WorkspaceSearchQuery() {
    }

    public static String cleanInput(String raw) {
        String value = raw == null ? "" : raw;
        while (value.startsWith("/")) {
            value = value.substring(1);
        }
        return value.length() > MAX_QUERY_LENGTH ? value.substring(0, MAX_QUERY_LENGTH) : value;
    }

    public static String normalized(String raw) {
        return cleanInput(raw == null ? "" : raw.trim()).toLowerCase(Locale.ROOT);
    }

    public static boolean matchesItem(
            String rawQuery,
            SlotWorkspaceViewModel.AtlasItem item,
            SlotWorkspaceViewModel.AtlasIsland island
    ) {
        String query = normalized(rawQuery);
        if (query.isBlank()) {
            return true;
        }
        if (item == null) {
            return false;
        }
        StringBuilder searchable = new StringBuilder();
        searchable.append(lower(item.name())).append(' ')
                .append(item.identity() == null ? "" : lower(item.identity().itemId())).append(' ');
        if (island != null) {
            searchable.append(lower(island.label())).append(' ');
            if (island.kind() != null) {
                searchable.append(lower(island.kind().name())).append(' ');
            }
        }
        return searchable.toString().contains(query);
    }

    public static boolean matchesContentSummary(
            String rawQuery,
            SlotWorkspaceViewModel.ChestContentSummary summary
    ) {
        String query = normalized(rawQuery);
        if (query.isBlank() || summary == null) {
            return false;
        }
        return (lower(summary.name()) + ' ' + lower(summary.itemId())).contains(query);
    }

    public static boolean matchesIdentityStack(String rawQuery, ItemIdentity identity, ItemStack stack) {
        String query = normalized(rawQuery);
        if (query.isBlank()) {
            return false;
        }
        StringBuilder searchable = new StringBuilder();
        if (identity != null) {
            searchable.append(lower(identity.itemId())).append(' ');
        }
        if (stack != null && !stack.isEmpty()) {
            searchable.append(lower(stack.getHoverName().getString()));
        }
        return searchable.toString().contains(query);
    }

    private static String lower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
