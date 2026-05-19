package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;

import java.util.Locale;
import java.util.Set;

/**
 * Hidden workflow-tab acceptance rule. Accepted inputs are relevant while a
 * tab is active, but they do not create wanted/desired pressure or missing
 * targets.
 */
public record WorkflowAcceptedInputRule(
        Kind kind,
        ItemIdentity identity,
        String tagId
) {
    public enum Kind {
        EXACT_ITEM,
        ITEM_TAG
    }

    public WorkflowAcceptedInputRule {
        kind = kind == null ? Kind.EXACT_ITEM : kind;
        if (kind == Kind.EXACT_ITEM) {
            if (identity == null) {
                throw new IllegalArgumentException("exact accepted input requires an identity");
            }
            tagId = "";
        } else {
            identity = null;
            tagId = normalizeTagId(tagId);
            if (tagId.isBlank()) {
                throw new IllegalArgumentException("tag accepted input requires a tag id");
            }
        }
    }

    public static WorkflowAcceptedInputRule exact(ItemIdentity identity) {
        return identity == null ? null : new WorkflowAcceptedInputRule(Kind.EXACT_ITEM, identity, "");
    }

    public static WorkflowAcceptedInputRule itemTag(String tagId) {
        String normalized = normalizeTagId(tagId);
        return normalized.isBlank() ? null : new WorkflowAcceptedInputRule(Kind.ITEM_TAG, null, normalized);
    }

    public boolean exactItem() {
        return kind == Kind.EXACT_ITEM;
    }

    public boolean itemTag() {
        return kind == Kind.ITEM_TAG;
    }

    public boolean matches(ItemIdentity candidate, Set<String> itemTags) {
        if (kind == Kind.EXACT_ITEM) {
            return candidate != null && ItemIdentityMatcher.matchesMovable(identity, candidate);
        }
        return itemTags != null && itemTags.contains(tagId);
    }

    public String displayLabel() {
        return kind == Kind.ITEM_TAG ? "#" + tagId : identity.itemId();
    }

    public static Kind parseKind(String value) {
        if (value == null || value.isBlank()) {
            return Kind.EXACT_ITEM;
        }
        try {
            return Kind.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return Kind.EXACT_ITEM;
        }
    }

    public static String normalizeTagId(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.startsWith("#")) {
            trimmed = trimmed.substring(1);
        }
        if (!trimmed.contains(":")) {
            return "";
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }
}
