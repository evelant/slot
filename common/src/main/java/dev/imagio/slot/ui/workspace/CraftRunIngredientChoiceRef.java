package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.workflow.domain.CraftRunAlternative;
import dev.imagio.slot.workflow.domain.CraftRunIngredientGroup;
import dev.imagio.slot.workflow.domain.CraftRunRecipeEntry;
import dev.imagio.slot.workflow.domain.CraftRunState;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public record CraftRunIngredientChoiceRef(String entryId, String groupId) {
    public static final String PLACEHOLDER_ITEM_ID = "slot:craft_run_choice";
    private static final String SEPARATOR = ".";

    public CraftRunIngredientChoiceRef {
        entryId = entryId == null ? "" : entryId.trim();
        groupId = groupId == null ? "" : groupId.trim();
    }

    public boolean present() {
        return !entryId.isBlank() && !groupId.isBlank();
    }

    public static ItemIdentity placeholderIdentity(String entryId, String groupId) {
        CraftRunIngredientChoiceRef ref = new CraftRunIngredientChoiceRef(entryId, groupId);
        if (!ref.present()) {
            return null;
        }
        return ItemIdentity.exact(PLACEHOLDER_ITEM_ID, encode(ref.entryId()) + SEPARATOR + encode(ref.groupId()));
    }

    public static boolean isPlaceholder(ItemIdentity identity) {
        return identity != null && PLACEHOLDER_ITEM_ID.equals(identity.itemId());
    }

    public static CraftRunIngredientChoiceRef fromIdentity(ItemIdentity identity) {
        if (!isPlaceholder(identity) || identity.componentFingerprint().isBlank()) {
            return null;
        }
        String fingerprint = identity.componentFingerprint();
        int separator = fingerprint.indexOf(SEPARATOR);
        if (separator <= 0 || separator >= fingerprint.length() - 1) {
            return null;
        }
        CraftRunIngredientChoiceRef ref = new CraftRunIngredientChoiceRef(
                decode(fingerprint.substring(0, separator)),
                decode(fingerprint.substring(separator + 1)));
        return ref.present() ? ref : null;
    }

    public static CraftRunIngredientChoiceRef forItem(
            CraftRunState run,
            SlotWorkspaceViewModel.AtlasItem item
    ) {
        ItemIdentity identity = item == null || item.identity() == null ? null : item.identity().toIdentity();
        if (identity == null || run == null || !run.active()) {
            return null;
        }
        CraftRunIngredientChoiceRef placeholder = fromIdentity(identity);
        if (placeholder != null) {
            return placeholder;
        }
        CraftRunIngredientChoiceRef match = null;
        for (CraftRunRecipeEntry entry : run.entries()) {
            if (entry == null || !entry.active()) {
                continue;
            }
            for (CraftRunIngredientGroup group : entry.inputs()) {
                if (group == null || group.alternatives().size() <= 1) {
                    continue;
                }
                for (CraftRunAlternative alternative : group.alternatives()) {
                    if (alternative == null
                            || alternative.identity() == null
                            || !ItemIdentityMatcher.matchesMovable(alternative.identity(), identity)) {
                        continue;
                    }
                    CraftRunIngredientChoiceRef next = new CraftRunIngredientChoiceRef(entry.entryId(), group.groupId());
                    if (match != null && !match.equals(next)) {
                        return null;
                    }
                    match = next;
                }
            }
        }
        return match;
    }

    public CraftRunRecipeEntry entry(CraftRunState run) {
        return run == null ? null : run.entry(entryId);
    }

    public CraftRunIngredientGroup group(CraftRunState run) {
        CraftRunRecipeEntry entry = entry(run);
        if (entry == null || !entry.active()) {
            return null;
        }
        for (CraftRunIngredientGroup group : entry.inputs()) {
            if (group != null && group.groupId().equals(groupId)) {
                return group;
            }
        }
        return null;
    }

    private static String encode(String value) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        try {
            return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ignored) {
            return "";
        }
    }
}
