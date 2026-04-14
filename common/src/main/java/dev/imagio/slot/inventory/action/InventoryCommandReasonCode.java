package dev.imagio.slot.inventory.action;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

public enum InventoryCommandReasonCode {
    MISSING_AUTHORITY,
    MISSING_VISIBLE_ROWS,
    MISSING_DESTINATION,
    INVALID_INTENT,
    MISSING_IDENTITY,
    NO_BACKING_ENTRIES,
    PLACEHOLDER_ONLY,
    NO_SELECTED_COLLECTION,
    NO_LOADOUT_ENTRIES,
    ALREADY_SELECTED,
    SOURCE_NOT_EXTRACTABLE,
    SOURCE_BLOCKED_BY_POLICY,
    DESTINATION_BLOCKED_BY_POLICY,
    DESTINATION_FULL,
    NO_DESTINATION_AVAILABLE,
    ENTRY_PARTIALLY_PLANNED,
    ROW_PARTIALLY_PLANNED,
    PROVIDER_CAPACITY_UNCERTAIN,
    UNSUPPORTED,
    NOT_VISIBLE_IN_SCOPE,
    NOT_RECENT,
    UNKNOWN;

    public static List<InventoryCommandReasonCode> fromDiagnostics(List<String> diagnostics) {
        if (diagnostics == null || diagnostics.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<InventoryCommandReasonCode> codes = new LinkedHashSet<>();
        for (String diagnostic : diagnostics) {
            InventoryCommandReasonCode code = fromDiagnostic(diagnostic);
            if (code != null) {
                codes.add(code);
            }
        }
        return List.copyOf(codes);
    }

    public static InventoryCommandReasonCode fromDiagnostic(String diagnostic) {
        if (diagnostic == null || diagnostic.isBlank()) {
            return null;
        }
        String normalized = diagnostic.toLowerCase(Locale.ROOT);
        if (normalized.contains("missing_authority")) {
            return MISSING_AUTHORITY;
        }
        if (normalized.contains("missing_visible_rows") || normalized.contains("anchor_row_not_visible")) {
            return MISSING_VISIBLE_ROWS;
        }
        if (normalized.contains("missing_destination") || normalized.contains("no_destination")) {
            return MISSING_DESTINATION;
        }
        if (normalized.contains("invalid_kind_scope_combination") || normalized.contains("missing_intent")) {
            return INVALID_INTENT;
        }
        if (normalized.contains("missing_identity")) {
            return MISSING_IDENTITY;
        }
        if (normalized.contains("no_backing_entries")) {
            return NO_BACKING_ENTRIES;
        }
        if (normalized.contains("placeholder")) {
            return PLACEHOLDER_ONLY;
        }
        if (normalized.contains("selected_collection")) {
            return NO_SELECTED_COLLECTION;
        }
        if (normalized.contains("loadout") && normalized.contains("empty")) {
            return NO_LOADOUT_ENTRIES;
        }
        if (normalized.contains("already_selected")) {
            return ALREADY_SELECTED;
        }
        if (normalized.contains("not_extractable")) {
            return SOURCE_NOT_EXTRACTABLE;
        }
        if (normalized.contains("blocked_by_policy") || normalized.contains("blocked_by_protection")) {
            return normalized.contains("destination")
                    ? DESTINATION_BLOCKED_BY_POLICY
                    : SOURCE_BLOCKED_BY_POLICY;
        }
        if (normalized.contains("destination_full")) {
            return DESTINATION_FULL;
        }
        if (normalized.contains("no_destination")) {
            return NO_DESTINATION_AVAILABLE;
        }
        if (normalized.contains("entry_partially_planned")) {
            return ENTRY_PARTIALLY_PLANNED;
        }
        if (normalized.contains("row_partially_planned")) {
            return ROW_PARTIALLY_PLANNED;
        }
        if (normalized.contains("capacity_uncertain")) {
            return PROVIDER_CAPACITY_UNCERTAIN;
        }
        if (normalized.contains("unsupported")) {
            return UNSUPPORTED;
        }
        if (normalized.contains("not_visible")) {
            return NOT_VISIBLE_IN_SCOPE;
        }
        if (normalized.contains("not_recent")) {
            return NOT_RECENT;
        }
        return UNKNOWN;
    }
}
