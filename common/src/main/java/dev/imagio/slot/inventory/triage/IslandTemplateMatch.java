package dev.imagio.slot.inventory.triage;

import java.util.Objects;
import java.util.Optional;

/**
 * Result of resolving a descriptor to a target island. Wraps either a
 * built-in {@link IslandSuggestionTemplate} (the role / class / tag
 * fallback) or a synthetic subsystem-named island that inherits
 * color and cluster placement from a parent template.
 *
 * <p>Subsystem matches let modded items group under "Create — Mechanical
 * Power" rather than collapsing into the catch-all MECHANISMS pile;
 * they only fire when a histogram-based qualifier deems the subsystem
 * "big enough" — small subsystems fall back to the parent template so
 * the atlas isn't fragmented into singleton islands.
 */
public final class IslandTemplateMatch {

    public static final String SUBSYSTEM_ISLAND_PREFIX = "subsystem:";

    private final IslandSuggestionTemplate parent;
    private final String subsystemId;
    private final String subsystemLabel;

    private IslandTemplateMatch(IslandSuggestionTemplate parent, String subsystemId, String subsystemLabel) {
        this.parent = Objects.requireNonNull(parent, "parent");
        this.subsystemId = subsystemId;
        this.subsystemLabel = subsystemLabel;
    }

    public static IslandTemplateMatch of(IslandSuggestionTemplate parent) {
        return new IslandTemplateMatch(parent, null, null);
    }

    public static IslandTemplateMatch subsystem(IslandSuggestionTemplate parent, String subsystemId, String label) {
        Objects.requireNonNull(subsystemId, "subsystemId");
        if (subsystemId.isBlank()) {
            throw new IllegalArgumentException("subsystemId must not be blank");
        }
        return new IslandTemplateMatch(parent, subsystemId, label == null || label.isBlank()
                ? formatSubsystemLabel(subsystemId)
                : label);
    }

    public IslandSuggestionTemplate parentTemplate() {
        return parent;
    }

    public String islandId() {
        return subsystemId == null ? parent.defaultIslandId() : SUBSYSTEM_ISLAND_PREFIX + subsystemId;
    }

    public String label() {
        return subsystemId == null ? parent.defaultLabel() : subsystemLabel;
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

    public boolean isSubsystem() {
        return subsystemId != null;
    }

    /**
     * Turn a subsystem id like {@code create:mechanical_power} into a
     * human-readable label {@code "Create — Mechanical Power"}.
     * Mirrors how players would name an island they built for that
     * subsystem.
     */
    public static String formatSubsystemLabel(String subsystemId) {
        if (subsystemId == null || subsystemId.isBlank()) {
            return "";
        }
        int colon = subsystemId.indexOf(':');
        String namespace = colon > 0 ? subsystemId.substring(0, colon) : "";
        String tail = colon >= 0 ? subsystemId.substring(colon + 1) : subsystemId;
        String namespacePart = capitalizeWords(namespace.replace('_', ' '));
        String tailPart = capitalizeWords(tail.replace('_', ' '));
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
                && Objects.equals(subsystemLabel, other.subsystemLabel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parent, subsystemId, subsystemLabel);
    }

    @Override
    public String toString() {
        return subsystemId == null
                ? "IslandTemplateMatch[" + parent.name() + "]"
                : "IslandTemplateMatch[" + parent.name() + ":subsystem=" + subsystemId + "]";
    }
}
