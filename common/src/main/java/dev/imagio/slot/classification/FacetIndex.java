package dev.imagio.slot.classification;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

public final class FacetIndex {

    public static volatile boolean ENABLED = true;

    static final Set<String> SUPPORTED_LAYERS = Set.of(
            "vanilla-base", "per-mod", "runtime-crawl", "modpack", "server", "player"
    );
    static final Pattern ITEM_ID_PATTERN = Pattern.compile("^[a-z0-9_.\\-]+:[a-z0-9_/.\\-]+$");
    static final int SUPPORTED_SCHEMA_VERSION = 1;

    private final Map<String, ItemFacets> entriesByItemId;

    private FacetIndex(Map<String, ItemFacets> entriesByItemId) {
        this.entriesByItemId = entriesByItemId;
    }

    public static FacetIndex empty() {
        return new FacetIndex(Map.of());
    }

    public static FacetIndex load(Reader reader) {
        JsonElement parsed = JsonParser.parseReader(reader);
        if (parsed == null || !parsed.isJsonObject()) {
            throw new IllegalArgumentException("classification layer JSON must be an object");
        }
        JsonObject root = parsed.getAsJsonObject();

        if (!root.has("schema_version") || !root.get("schema_version").isJsonPrimitive()) {
            throw new IllegalArgumentException("classification layer missing schema_version");
        }
        int schemaVersion = root.get("schema_version").getAsInt();
        if (schemaVersion != SUPPORTED_SCHEMA_VERSION) {
            throw new IllegalArgumentException("unsupported classification schema_version: " + schemaVersion);
        }

        if (!root.has("layer") || !root.get("layer").isJsonPrimitive()) {
            throw new IllegalArgumentException("classification layer missing layer");
        }
        String layer = root.get("layer").getAsString();
        if (!SUPPORTED_LAYERS.contains(layer)) {
            throw new IllegalArgumentException("unknown classification layer name: " + layer);
        }

        if (!root.has("entries") || !root.get("entries").isJsonObject()) {
            return new FacetIndex(Map.of());
        }
        JsonObject entries = root.getAsJsonObject("entries");

        Map<String, ItemFacets> facets = new LinkedHashMap<>(entries.size());
        for (Map.Entry<String, JsonElement> entry : entries.entrySet()) {
            String itemId = entry.getKey();
            if (!ITEM_ID_PATTERN.matcher(itemId).matches()) {
                continue;
            }
            JsonElement entryValue = entry.getValue();
            if (entryValue == null || !entryValue.isJsonObject()) {
                continue;
            }
            JsonObject entryObj = entryValue.getAsJsonObject();
            if (!entryObj.has("facets") || !entryObj.get("facets").isJsonObject()) {
                continue;
            }
            JsonObject facetsObj = entryObj.getAsJsonObject("facets");
            List<String> roleCandidates = readAllStringFacetValues(facetsObj, "role");
            String role = roleCandidates.isEmpty() ? null : roleCandidates.get(0);
            String materialFamily = readSingleStringFacet(facetsObj, "material_family");
            List<String> workflows = readMultiStringFacet(facetsObj, "workflow");
            List<String> workflowRoles = readMultiStringFacet(facetsObj, "workflow_role");
            List<String> usedAt = readMultiStringFacet(facetsObj, "used_at");
            List<String> processingIn = readMultiStringFacet(facetsObj, "processing_in");
            List<String> subsystems = readMultiStringFacet(facetsObj, "mod_subsystem");
            List<String> organizationGroups = readMultiStringFacet(facetsObj, "organization_group");
            List<String> activities = readMultiStringFacet(facetsObj, "activity");
            List<String> primaryUses = readMultiStringFacet(facetsObj, "primary_uses");
            String flavor = readSingleStringFacet(facetsObj, "flavor");
            String carryFrequency = readSingleStringFacet(facetsObj, "carry_frequency");
            String rarity = readSingleStringFacet(facetsObj, "rarity");
            String origin = readSingleStringFacet(facetsObj, "origin");
            String dyeColor = readSingleStringFacet(facetsObj, "dye_color");
            List<String> palette = readMultiStringFacet(facetsObj, "palette");
            String form = readSingleStringFacet(facetsObj, "form");
            boolean emitsLight = readBooleanFacet(facetsObj, "emits_light");
            boolean isFuel = readBooleanFacet(facetsObj, "is_fuel");

            boolean hasAnyFacet = role != null
                    || materialFamily != null
                    || !workflows.isEmpty()
                    || !workflowRoles.isEmpty()
                    || !usedAt.isEmpty()
                    || !processingIn.isEmpty()
                    || !subsystems.isEmpty()
                    || !organizationGroups.isEmpty()
                    || !activities.isEmpty()
                    || !primaryUses.isEmpty()
                    || flavor != null
                    || carryFrequency != null
                    || rarity != null
                    || origin != null
                    || dyeColor != null
                    || !palette.isEmpty()
                    || form != null
                    || emitsLight
                    || isFuel;
            if (hasAnyFacet) {
                facets.put(itemId, new ItemFacets(
                        role,
                        roleCandidates,
                        materialFamily,
                        workflows,
                        workflowRoles,
                        usedAt,
                        processingIn,
                        subsystems,
                        organizationGroups,
                        activities,
                        primaryUses,
                        flavor,
                        carryFrequency,
                        rarity,
                        origin,
                        dyeColor,
                        palette,
                        form,
                        emitsLight,
                        isFuel
                ));
            }
        }
        return new FacetIndex(Collections.unmodifiableMap(facets));
    }

    /**
     * Return a new index whose entries are the union of {@code this} and
     * {@code other}, with {@code other}'s entries winning on conflict. We
     * use this to layer per-mod data on top of {@code vanilla-base} at
     * boot — modded namespaces (create:cogwheel etc.) live in per-mod
     * files and shouldn't be in vanilla-base, so the conflict path is
     * mostly defensive (e.g. if a future per-mod layer corrects a
     * vanilla entry).
     */
    public FacetIndex mergedWith(FacetIndex other) {
        if (other == null || other.isEmpty()) {
            return this;
        }
        if (this.isEmpty()) {
            return other;
        }
        Map<String, ItemFacets> merged = new LinkedHashMap<>(entriesByItemId);
        merged.putAll(other.entriesByItemId);
        return new FacetIndex(Collections.unmodifiableMap(merged));
    }

    public Optional<String> role(String itemId) {
        ItemFacets f = lookup(itemId);
        return f == null ? Optional.empty() : Optional.ofNullable(f.role());
    }

    /**
     * All candidate role values, primary first. Returns
     * {@code [role]} for items with a single-value role facet, the full
     * list for ambiguous role facets ({@code track_signal} →
     * {@code [mechanism, redstone_component]}), or an empty list when
     * the item has no role at all. Activity-driven tie-breaking in
     * {@link dev.imagio.slot.inventory.triage.IslandSuggestionTemplate}
     * uses this to consider every candidate template, not just the
     * first.
     */
    public List<String> roleAlternatives(String itemId) {
        ItemFacets f = lookup(itemId);
        return f == null ? List.of() : f.roleAlternatives();
    }

    public Optional<String> materialFamily(String itemId) {
        ItemFacets f = lookup(itemId);
        return f == null ? Optional.empty() : Optional.ofNullable(f.materialFamily());
    }

    public List<String> workflows(String itemId) {
        ItemFacets f = lookup(itemId);
        return f == null ? List.of() : f.workflows();
    }

    public List<String> workflowRoles(String itemId) {
        ItemFacets f = lookup(itemId);
        return f == null ? List.of() : f.workflowRoles();
    }

    public List<String> usedAt(String itemId) {
        ItemFacets f = lookup(itemId);
        return f == null ? List.of() : f.usedAt();
    }

    public List<String> processingIn(String itemId) {
        ItemFacets f = lookup(itemId);
        return f == null ? List.of() : f.processingIn();
    }

    public List<String> subsystems(String itemId) {
        ItemFacets f = lookup(itemId);
        return f == null ? List.of() : f.subsystems();
    }

    public List<String> organizationGroups(String itemId) {
        ItemFacets f = lookup(itemId);
        return f == null ? List.of() : f.organizationGroups();
    }

    public List<String> activities(String itemId) {
        ItemFacets f = lookup(itemId);
        return f == null ? List.of() : f.activities();
    }

    public List<String> primaryUses(String itemId) {
        ItemFacets f = lookup(itemId);
        return f == null ? List.of() : f.primaryUses();
    }

    public Optional<String> flavor(String itemId) {
        ItemFacets f = lookup(itemId);
        return f == null ? Optional.empty() : Optional.ofNullable(f.flavor());
    }

    /**
     * How often this item lives in a player's carried inventory during
     * normal play — distinct from {@link #rarity} (world-abundance).
     * Drives the populate generator's "what would a real player pack"
     * weighting and the within-island carry-rank sort.
     */
    public Optional<String> carryFrequency(String itemId) {
        ItemFacets f = lookup(itemId);
        return f == null ? Optional.empty() : Optional.ofNullable(f.carryFrequency());
    }

    public Optional<String> rarity(String itemId) {
        ItemFacets f = lookup(itemId);
        return f == null ? Optional.empty() : Optional.ofNullable(f.rarity());
    }

    public Optional<String> origin(String itemId) {
        ItemFacets f = lookup(itemId);
        return f == null ? Optional.empty() : Optional.ofNullable(f.origin());
    }

    public Optional<String> dyeColor(String itemId) {
        ItemFacets f = lookup(itemId);
        return f == null ? Optional.empty() : Optional.ofNullable(f.dyeColor());
    }

    /**
     * Color/tone bucket(s) for items the LLM tagged with a {@code palette}
     * facet — {@code wood_red}, {@code wood_medium}, {@code copper_bright},
     * {@code earthy}, {@code warm}, etc. Multi-value because some items
     * share more than one bucket (e.g. {@code [wood_red, glossy]}).
     * Drives the within-island sub-cluster when {@link #dyeColor} isn't
     * set, so non-dyed BUILDING / DECORATION items still group by visual
     * tone instead of pure id-alphabetical.
     */
    public List<String> palette(String itemId) {
        ItemFacets f = lookup(itemId);
        return f == null ? List.of() : f.palette();
    }

    /**
     * True when this item emits light when placed (or in some hand-held
     * cases). Drives the dedicated Lighting island so torches /
     * lanterns / glowstone / shroomlight cluster together regardless
     * of their underlying role (utility / decorative_block / etc.).
     */
    public boolean emitsLight(String itemId) {
        ItemFacets f = lookup(itemId);
        return f != null && f.emitsLight();
    }

    public boolean isFuel(String itemId) {
        ItemFacets f = lookup(itemId);
        return f != null && f.isFuel();
    }

    /**
     * Shape facet — {@code stairs}, {@code slab}, {@code wall},
     * {@code door}, {@code trapdoor}, {@code fence}, {@code fence_gate},
     * {@code pane}, {@code bars}, {@code ingot}, {@code gem},
     * {@code whole_block}, etc. Drives form-keyed templates so a "stairs"
     * item routes to the dedicated Stairs island instead of the generic
     * Building Blocks pile.
     */
    public Optional<String> form(String itemId) {
        ItemFacets f = lookup(itemId);
        return f == null ? Optional.empty() : Optional.ofNullable(f.form());
    }

    private ItemFacets lookup(String itemId) {
        if (itemId == null || itemId.isEmpty()) {
            return null;
        }
        return entriesByItemId.get(itemId);
    }

    public int size() {
        return entriesByItemId.size();
    }

    public boolean isEmpty() {
        return entriesByItemId.isEmpty();
    }

    public Set<String> itemIds() {
        return entriesByItemId.keySet();
    }

    private record ItemFacets(
            String role,
            List<String> roleAlternatives,
            String materialFamily,
            List<String> workflows,
            List<String> workflowRoles,
            List<String> usedAt,
            List<String> processingIn,
            List<String> subsystems,
            List<String> organizationGroups,
            List<String> activities,
            List<String> primaryUses,
            String flavor,
            String carryFrequency,
            String rarity,
            String origin,
            String dyeColor,
            List<String> palette,
            String form,
            boolean emitsLight,
            boolean isFuel
    ) {
        ItemFacets {
            roleAlternatives = roleAlternatives == null ? List.of() : List.copyOf(roleAlternatives);
            workflows = workflows == null ? List.of() : List.copyOf(workflows);
            workflowRoles = workflowRoles == null ? List.of() : List.copyOf(workflowRoles);
            usedAt = usedAt == null ? List.of() : List.copyOf(usedAt);
            processingIn = processingIn == null ? List.of() : List.copyOf(processingIn);
            subsystems = subsystems == null ? List.of() : List.copyOf(subsystems);
            organizationGroups = organizationGroups == null ? List.of() : List.copyOf(organizationGroups);
            activities = activities == null ? List.of() : List.copyOf(activities);
            primaryUses = primaryUses == null ? List.of() : List.copyOf(primaryUses);
            palette = palette == null ? List.of() : List.copyOf(palette);
        }
    }

    /**
     * Read a facet that's conceptually single-valued. Accepts either the
     * canonical {@code {value: "x"}} shape, or the multi-value
     * {@code {values: [...]}} shape (preferring the first non-null entry)
     * — a few facets are nominally single but the LLM occasionally emits a
     * list (e.g. {@code role} for {@code track_signal}).
     */
    private static String readSingleStringFacet(JsonObject facets, String facetName) {
        if (!facets.has(facetName)) {
            return null;
        }
        JsonElement raw = facets.get(facetName);
        if (raw == null || !raw.isJsonObject()) {
            return null;
        }
        JsonObject facet = raw.getAsJsonObject();
        if (facet.has("value")) {
            JsonElement value = facet.get("value");
            if (value == null || value.isJsonNull()) {
                return null;
            }
            return value.isJsonPrimitive() ? value.getAsString() : null;
        }
        if (facet.has("values") && facet.get("values").isJsonArray()) {
            JsonArray values = facet.getAsJsonArray("values");
            for (JsonElement element : values) {
                if (element != null && element.isJsonPrimitive()) {
                    String s = element.getAsString();
                    if (s != null && !s.isBlank()) {
                        return s;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Like {@link #readSingleStringFacet} but returns the full list of
     * candidate values rather than just the first. Used for facets that
     * are conceptually single but where the LLM emits an ambiguous list
     * (e.g. {@code role} for {@code track_signal} →
     * {@code [mechanism, redstone_component]}). The first entry is the
     * primary value; remaining entries are alternatives that
     * downstream tie-breakers can consider.
     */
    private static List<String> readAllStringFacetValues(JsonObject facets, String facetName) {
        if (!facets.has(facetName)) {
            return List.of();
        }
        JsonElement raw = facets.get(facetName);
        if (raw == null || !raw.isJsonObject()) {
            return List.of();
        }
        JsonObject facet = raw.getAsJsonObject();
        ArrayList<String> out = new ArrayList<>(2);
        if (facet.has("value")) {
            JsonElement value = facet.get("value");
            if (value != null && value.isJsonPrimitive()) {
                String s = value.getAsString();
                if (s != null && !s.isBlank()) {
                    out.add(s);
                }
            }
        }
        if (facet.has("values") && facet.get("values").isJsonArray()) {
            JsonArray values = facet.getAsJsonArray("values");
            for (JsonElement element : values) {
                if (element != null && element.isJsonPrimitive()) {
                    String s = element.getAsString();
                    if (s != null && !s.isBlank() && !out.contains(s)) {
                        out.add(s);
                    }
                }
            }
        }
        return out.isEmpty() ? List.of() : List.copyOf(out);
    }

    /**
     * Read a boolean facet. Returns {@code false} when missing,
     * non-object, or anything other than {@code {value: true}} —
     * boolean facets are absent-or-true, not absent-or-false-or-true.
     */
    private static boolean readBooleanFacet(JsonObject facets, String facetName) {
        if (!facets.has(facetName)) {
            return false;
        }
        JsonElement raw = facets.get(facetName);
        if (raw == null || !raw.isJsonObject()) {
            return false;
        }
        JsonObject facet = raw.getAsJsonObject();
        if (!facet.has("value")) {
            return false;
        }
        JsonElement value = facet.get("value");
        if (value == null || value.isJsonNull() || !value.isJsonPrimitive()) {
            return false;
        }
        return value.getAsJsonPrimitive().isBoolean()
                ? value.getAsBoolean()
                : false;
    }

    /**
     * Read a facet that's conceptually multi-valued. Accepts the canonical
     * {@code {values: [...]}} shape; also tolerates a degenerate
     * {@code {value: "x"}} shape by treating it as a single-element list.
     */
    private static List<String> readMultiStringFacet(JsonObject facets, String facetName) {
        if (!facets.has(facetName)) {
            return List.of();
        }
        JsonElement raw = facets.get(facetName);
        if (raw == null || !raw.isJsonObject()) {
            return List.of();
        }
        JsonObject facet = raw.getAsJsonObject();
        if (facet.has("values") && facet.get("values").isJsonArray()) {
            JsonArray values = facet.getAsJsonArray("values");
            ArrayList<String> out = new ArrayList<>(values.size());
            for (JsonElement element : values) {
                if (element != null && element.isJsonPrimitive()) {
                    String s = element.getAsString();
                    if (s != null && !s.isBlank() && !out.contains(s)) {
                        out.add(s);
                    }
                }
            }
            return out.isEmpty() ? List.of() : List.copyOf(out);
        }
        if (facet.has("value")) {
            JsonElement value = facet.get("value");
            if (value != null && value.isJsonPrimitive()) {
                String s = value.getAsString();
                if (s != null && !s.isBlank()) {
                    return List.of(s);
                }
            }
        }
        return List.of();
    }
}
