package dev.imagio.slot.inventory.triage;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.imagio.slot.classification.FacetIndex;
import dev.imagio.slot.classification.FacetIndexBootstrap;
import dev.imagio.slot.inventory.core.ItemIdentity;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Locks the chip-coverage floor against the bundled vanilla dataset.
 * Every entry that has a `role` facet must resolve to some
 * {@link IslandSuggestionTemplate} via {@link IslandSuggestionTemplate#firstMatch}
 * — the contract being "every classified vanilla item gets a chip."
 *
 * <p>If a future schema change adds a new role value without wiring a
 * template trigger, this test surfaces the gap immediately.
 */
class IslandSuggestionTemplateCoverageTest {

    @Test
    void everyVanillaRoleResolvesToATemplate() throws Exception {
        Map<String, String> rolesByItemId = loadVanillaRoles();
        assertTrue(rolesByItemId.size() >= 1000,
                "bundled vanilla dataset should expose at least 1000 role-bearing entries; got "
                        + rolesByItemId.size());

        java.util.Set<String> unmatchedRoles = new java.util.LinkedHashSet<>();
        java.util.LinkedHashMap<String, Integer> coverageByTemplate = new java.util.LinkedHashMap<>();

        for (Map.Entry<String, String> entry : rolesByItemId.entrySet()) {
            String itemId = entry.getKey();
            String role = entry.getValue();
            IslandSignalDescriptor descriptor = new IslandSignalDescriptor(
                    ItemIdentity.of(itemId),
                    Set.of(),
                    Set.of(),
                    namespaceOf(itemId),
                    "",
                    role,
                    null
            );
            IslandSuggestionTemplate match = IslandSuggestionTemplate.firstMatch(descriptor);
            if (match == null) {
                unmatchedRoles.add(role);
            } else {
                coverageByTemplate.merge(match.name(), 1, Integer::sum);
            }
        }

        assertTrue(unmatchedRoles.isEmpty(),
                "every role in the vanilla dataset must map to at least one template — unmatched: "
                        + unmatchedRoles);
    }

    @Test
    void firstMatchOrMiscNeverReturnsNullForVanillaItems() throws Exception {
        Map<String, String> rolesByItemId = loadVanillaRoles();
        for (Map.Entry<String, String> entry : rolesByItemId.entrySet()) {
            IslandSignalDescriptor descriptor = new IslandSignalDescriptor(
                    ItemIdentity.of(entry.getKey()),
                    Set.of(),
                    Set.of(),
                    namespaceOf(entry.getKey()),
                    "",
                    entry.getValue(),
                    null
            );
            IslandSuggestionTemplate match = IslandSuggestionTemplate.firstMatchOrMisc(descriptor);
            assertNotNull(match, "firstMatchOrMisc must always return a template");
        }
    }

    @Test
    void shippedDatasetClassifiesPlaytestProblemItemsCorrectly() throws Exception {
        // Locks in fixes for the specific items playtest surfaced as
        // misclassified. If a future regeneration regresses any of
        // these, the test points exactly at what flipped.
        Map<String, IslandSuggestionTemplate> expected = new java.util.LinkedHashMap<>();
        // Doors / trapdoors / fence_gates → DOORS (split out of
        // BUILDING by form-keyed template; players think of doors as
        // their own category).
        expected.put("minecraft:jungle_door", IslandSuggestionTemplate.DOORS);
        expected.put("minecraft:iron_door", IslandSuggestionTemplate.DOORS);
        expected.put("minecraft:oxidized_copper_door", IslandSuggestionTemplate.DOORS);
        expected.put("minecraft:exposed_copper_door", IslandSuggestionTemplate.DOORS);
        expected.put("minecraft:oak_trapdoor", IslandSuggestionTemplate.DOORS);
        expected.put("minecraft:iron_trapdoor", IslandSuggestionTemplate.DOORS);
        expected.put("minecraft:oak_fence_gate", IslandSuggestionTemplate.DOORS);
        // Beds + decorated_pot → DECORATION (was functional_block).
        expected.put("minecraft:brown_bed", IslandSuggestionTemplate.DECORATION);
        expected.put("minecraft:red_bed", IslandSuggestionTemplate.DECORATION);
        expected.put("minecraft:decorated_pot", IslandSuggestionTemplate.DECORATION);
        // Compressed material blocks → RAW_MATERIALS (the player's
        // "ore stockpile" island; they live with raw_iron / raw_copper /
        // raw_gold rather than the broader Materials catch-all).
        expected.put("minecraft:diamond_block", IslandSuggestionTemplate.RAW_MATERIALS);
        expected.put("minecraft:iron_block", IslandSuggestionTemplate.RAW_MATERIALS);
        expected.put("minecraft:gold_block", IslandSuggestionTemplate.RAW_MATERIALS);
        expected.put("minecraft:emerald_block", IslandSuggestionTemplate.RAW_MATERIALS);
        expected.put("minecraft:copper_block", IslandSuggestionTemplate.RAW_MATERIALS);
        // Rails → TRANSPORT (was functional_block / redstone_component).
        expected.put("minecraft:rail", IslandSuggestionTemplate.TRANSPORT);
        expected.put("minecraft:powered_rail", IslandSuggestionTemplate.TRANSPORT);
        expected.put("minecraft:detector_rail", IslandSuggestionTemplate.TRANSPORT);
        expected.put("minecraft:activator_rail", IslandSuggestionTemplate.TRANSPORT);
        // Spawn eggs → CURIOSITY (was utility / admin).
        expected.put("minecraft:bee_spawn_egg", IslandSuggestionTemplate.CURIOSITY);
        expected.put("minecraft:zombie_spawn_egg", IslandSuggestionTemplate.CURIOSITY);
        // Mob drops + raw ores → MATERIALS (was natural_resource).
        expected.put("minecraft:blaze_rod", IslandSuggestionTemplate.MATERIALS);
        expected.put("minecraft:string", IslandSuggestionTemplate.MATERIALS);
        expected.put("minecraft:leather", IslandSuggestionTemplate.MATERIALS);
        expected.put("minecraft:feather", IslandSuggestionTemplate.MATERIALS);
        expected.put("minecraft:bone", IslandSuggestionTemplate.MATERIALS);
        expected.put("minecraft:slime_ball", IslandSuggestionTemplate.MATERIALS);
        expected.put("minecraft:raw_iron", IslandSuggestionTemplate.MATERIALS);

        Map<String, String> rolesByItemId = loadVanillaRoles();
        for (Map.Entry<String, IslandSuggestionTemplate> e : expected.entrySet()) {
            String role = rolesByItemId.get(e.getKey());
            assertNotNull(role, "expected role facet for " + e.getKey());
            IslandSignalDescriptor descriptor = new IslandSignalDescriptor(
                    ItemIdentity.of(e.getKey()), Set.of(), Set.of(),
                    namespaceOf(e.getKey()), "", role, null);
            IslandSuggestionTemplate match = IslandSuggestionTemplate.firstMatch(descriptor);
            org.junit.jupiter.api.Assertions.assertEquals(e.getValue(), match,
                    e.getKey() + " (role=" + role + ") should resolve to " + e.getValue());
        }
    }

    @Test
    void naturalResourceRoutesToNaturalNotMaterials() {
        IslandSignalDescriptor descriptor = new IslandSignalDescriptor(
                ItemIdentity.of("minecraft:birch_wood"),
                Set.of(),
                Set.of(),
                "minecraft",
                "",
                "natural_resource",
                "wood_birch"
        );
        IslandSuggestionTemplate match = IslandSuggestionTemplate.firstMatch(descriptor);
        assertNotNull(match);
        // Regression: prior to the overlap fix, MATERIALS captured every
        // natural_resource item because it was listed in both MATERIALS
        // and NATURAL roleTriggers. NATURAL must win now.
        org.junit.jupiter.api.Assertions.assertEquals(IslandSuggestionTemplate.NATURAL, match);
    }

    @Test
    void bundledPerModDataExposesQualifiedSubsystems() {
        // Phase 2 acceptance: when the bundled per-mod data is loaded,
        // a histogram across the dataset should show the major Create
        // subsystems clearing the threshold (=4). This locks the dataset
        // shape: subsystem-aware islands are realistic for the modpacks
        // we ship with — not aspirational.
        FacetIndex index = FacetIndexBootstrap.loadAll();
        assertTrue(index.size() > 1500, "bundled per-mod data must be loaded");

        // The dataset itself; exercising index.subsystems() across every
        // item builds the same histogram the generator does at runtime.
        HashMap<String, Integer> hist = new HashMap<>();
        try (InputStream stream = FacetIndexBootstrap.class.getResourceAsStream(
                FacetIndexBootstrap.PER_MOD_INDEX_RESOURCE);
             Reader manifestReader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            JsonObject manifest = JsonParser.parseReader(manifestReader).getAsJsonObject();
            for (JsonElement m : manifest.getAsJsonArray("mods")) {
                String modId = m.getAsString();
                try (InputStream perMod = FacetIndexBootstrap.class.getResourceAsStream(
                        FacetIndexBootstrap.PER_MOD_RESOURCE_PREFIX + modId + ".json");
                     Reader perModReader = new InputStreamReader(perMod, StandardCharsets.UTF_8)) {
                    JsonObject root = JsonParser.parseReader(perModReader).getAsJsonObject();
                    JsonObject entries = root.getAsJsonObject("entries");
                    for (Map.Entry<String, JsonElement> entry : entries.entrySet()) {
                        List<String> subs = index.subsystems(entry.getKey());
                        for (String s : subs) {
                            hist.merge(s, 1, Integer::sum);
                        }
                    }
                }
            }
        } catch (Exception e) {
            org.junit.jupiter.api.Assertions.fail("failed to read bundled per-mod data", e);
        }

        // The Create subsystems whose parent template (MECHANISMS /
        // WORKBENCHES / TRANSPORT) honors subsystem grouping must clear
        // the 10-item threshold. If a regeneration drops one of these
        // below 10, the populate atlas silently loses its dedicated
        // island — fail loudly here so we notice.
        Set<String> expectedQualified = Set.of(
                "create:mechanical_power",
                "create:logistics",
                "create:contraptions"
        );
        for (String subsystemId : expectedQualified) {
            Integer count = hist.get(subsystemId);
            assertNotNull(count,
                    "expected subsystem " + subsystemId + " to be present in bundled data");
            assertTrue(count >= 10,
                    "expected subsystem " + subsystemId + " to have ≥10 items; got " + count);
        }
    }

    private static Map<String, String> loadVanillaRoles() throws Exception {
        try (InputStream in = FacetIndexBootstrap.class.getResourceAsStream(
                FacetIndexBootstrap.VANILLA_BASE_RESOURCE)) {
            assertNotNull(in, "bundled vanilla-base.json must exist on classpath");
            try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                JsonObject entries = root.getAsJsonObject("entries");
                LinkedHashMap<String, String> out = new LinkedHashMap<>();
                for (Map.Entry<String, JsonElement> entry : entries.entrySet()) {
                    JsonObject entryObj = entry.getValue().getAsJsonObject();
                    if (!entryObj.has("facets")) {
                        continue;
                    }
                    JsonObject facets = entryObj.getAsJsonObject("facets");
                    if (!facets.has("role")) {
                        continue;
                    }
                    JsonObject roleFacet = facets.getAsJsonObject("role");
                    if (!roleFacet.has("value")) {
                        continue;
                    }
                    JsonElement value = roleFacet.get("value");
                    if (value.isJsonNull() || !value.isJsonPrimitive()) {
                        continue;
                    }
                    out.put(entry.getKey(), value.getAsString());
                }
                return out;
            }
        }
    }

    private static String namespaceOf(String itemId) {
        int colon = itemId.indexOf(':');
        return colon <= 0 ? "" : itemId.substring(0, colon);
    }
}
