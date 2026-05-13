package dev.imagio.slot.inventory.triage;

import java.util.Objects;
import java.util.Optional;

/**
 * Result of resolving a descriptor to a target island. Wraps either a
 * built-in {@link IslandSuggestionTemplate} (the role / class / tag
 * fallback) or a synthetic classifier-named island that inherits color and
 * cluster placement from a parent template.
 *
 * <p>Organization-group matches let modded items group under player storage
 * buckets such as "Casting Molds" or "Masonry Supplies" rather than
 * collapsing into broad MATERIALS / UTILITY piles. Mod-subsystem matches are
 * still represented for existing/manual subsystem islands and diagnostics, but
 * templates do not currently auto-create subsystem main-wall sections.
 */
public final class IslandTemplateMatch {

    public static final String SUBSYSTEM_ISLAND_PREFIX = "subsystem:";
    public static final String ORGANIZATION_GROUP_ISLAND_PREFIX = "group:";

    private final IslandSuggestionTemplate parent;
    private final String subsystemId;
    private final String organizationGroupId;
    private final String dynamicLabel;

    private IslandTemplateMatch(
            IslandSuggestionTemplate parent,
            String subsystemId,
            String organizationGroupId,
            String dynamicLabel
    ) {
        this.parent = Objects.requireNonNull(parent, "parent");
        this.subsystemId = subsystemId;
        this.organizationGroupId = organizationGroupId;
        this.dynamicLabel = dynamicLabel;
    }

    public static IslandTemplateMatch of(IslandSuggestionTemplate parent) {
        return new IslandTemplateMatch(parent, null, null, null);
    }

    public static IslandTemplateMatch subsystem(IslandSuggestionTemplate parent, String subsystemId, String label) {
        Objects.requireNonNull(subsystemId, "subsystemId");
        if (subsystemId.isBlank()) {
            throw new IllegalArgumentException("subsystemId must not be blank");
        }
        return new IslandTemplateMatch(parent, subsystemId, null, label == null || label.isBlank()
                ? formatSubsystemLabel(subsystemId)
                : label);
    }

    public static IslandTemplateMatch organizationGroup(
            IslandSuggestionTemplate parent,
            String organizationGroupId,
            String label
    ) {
        Objects.requireNonNull(organizationGroupId, "organizationGroupId");
        if (organizationGroupId.isBlank()) {
            throw new IllegalArgumentException("organizationGroupId must not be blank");
        }
        return new IslandTemplateMatch(parent, null, organizationGroupId, label == null || label.isBlank()
                ? formatSubsystemLabel(organizationGroupId)
                : label);
    }

    public IslandSuggestionTemplate parentTemplate() {
        return parent;
    }

    public String islandId() {
        if (organizationGroupId != null) {
            return ORGANIZATION_GROUP_ISLAND_PREFIX + organizationGroupId;
        }
        return subsystemId == null ? parent.defaultIslandId() : SUBSYSTEM_ISLAND_PREFIX + subsystemId;
    }

    public String label() {
        return isDynamic() ? dynamicLabel : parent.defaultLabel();
    }

    public int color() {
        return parent.defaultColor();
    }

    public int clusterRow() {
        return parent.clusterRow();
    }

    public int clusterColumn() {
        return parent.clusterColumn();
    }

    public Optional<String> subsystemId() {
        return Optional.ofNullable(subsystemId);
    }

    public Optional<String> organizationGroupId() {
        return Optional.ofNullable(organizationGroupId);
    }

    public boolean isSubsystem() {
        return subsystemId != null;
    }

    public boolean isOrganizationGroup() {
        return organizationGroupId != null;
    }

    public boolean isDynamic() {
        return subsystemId != null || organizationGroupId != null;
    }

    /**
     * Turn a namespaced classifier id like {@code create:mechanical_power} into
     * a human-readable label {@code "Create — Mechanical Power"}. Mirrors how
     * players would name an island they built for that group.
     */
    public static String formatSubsystemLabel(String subsystemId) {
        if (subsystemId == null || subsystemId.isBlank()) {
            return "";
        }
        int colon = subsystemId.indexOf(':');
        String namespace = colon > 0 ? subsystemId.substring(0, colon) : "";
        String tail = colon >= 0 ? subsystemId.substring(colon + 1) : subsystemId;
        if ("pack".equals(namespace)) {
            int slash = tail.indexOf('/');
            if (slash >= 0 && slash < tail.length() - 1) {
                tail = tail.substring(slash + 1);
            }
            namespace = "";
        }
        String namespacePart = capitalizeWords(namespace.replace('_', ' '));
        String tailPart = capitalizeWords(tail.replace('_', ' ').replace('/', ' '));
        if (namespacePart.isEmpty()) {
            return tailPart;
        }
        if (tailPart.isEmpty()) {
            return namespacePart;
        }
        return namespacePart + " — " + tailPart;
    }

    private static String capitalizeWords(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        StringBuilder out = new StringBuilder(input.length());
        boolean atWordStart = true;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (Character.isWhitespace(c)) {
                out.append(c);
                atWordStart = true;
            } else if (atWordStart) {
                out.append(Character.toUpperCase(c));
                atWordStart = false;
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof IslandTemplateMatch other)) {
            return false;
        }
        return parent == other.parent
                && Objects.equals(subsystemId, other.subsystemId)
                && Objects.equals(organizationGroupId, other.organizationGroupId)
                && Objects.equals(dynamicLabel, other.dynamicLabel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parent, subsystemId, organizationGroupId, dynamicLabel);
    }

    @Override
    public String toString() {
        if (organizationGroupId != null) {
            return "IslandTemplateMatch[" + parent.name() + ":group=" + organizationGroupId + "]";
        }
        return subsystemId == null
                ? "IslandTemplateMatch[" + parent.name() + "]"
                : "IslandTemplateMatch[" + parent.name() + ":subsystem=" + subsystemId + "]";
    }
}
