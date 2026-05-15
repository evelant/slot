package dev.imagio.slot.inventory.triage;

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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        FacetIndex index = FacetIndexBootstrap.loadVanillaBase();
        assertTrue(index.itemIds().size() >= 1000,
                "bundled vanilla dataset should expose at least 1000 role-bearing entries; got "
                        + index.itemIds().size());

        java.util.Set<String> unmatchedRoles = new java.util.LinkedHashSet<>();
        LinkedHashMap<String, Integer> coverageByTemplate = new LinkedHashMap<>();

        for (String itemId : index.itemIds()) {
            String role = index.role(itemId).orElse(null);
            if (role == null) {
                continue;
            }
            IslandSignalDescriptor descriptor = descriptorFrom(index, itemId);
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
        FacetIndex index = FacetIndexBootstrap.loadVanillaBase();
        for (String itemId : index.itemIds()) {
            IslandSignalDescriptor descriptor = descriptorFrom(index, itemId);
            IslandSuggestionTemplate match = IslandSuggestionTemplate.firstMatchOrMisc(descriptor);
            assertNotNull(match, "firstMatchOrMisc must always return a template");
        }
    }

    @Test
    void shippedDatasetClassifiesPlaytestProblemItemsCorrectly() throws Exception {
        // Locks in fixes for the specific items playtest surfaced as
        // misclassified. If a future regeneration regresses any of
        // these, the test points exactly at what flipped.
        Map<String, IslandSuggestionTemplate> expected = new LinkedHashMap<>();
        // Doors / trapdoors / fence_gates → DOORS (split out of
        // BUILDING by form-keyed template; players think of doors as
        // their own category).
        expected.put("minecraft:jungle_door", IslandSuggestionTemplate.DOORS);
        expected.put("minecraft:iron_door", IslandSuggestionTemplate.DOORS);
        expected.put("minecraft:oak_trapdoor", IslandSuggestionTemplate.DOORS);
        expected.put("minecraft:iron_trapdoor", IslandSuggestionTemplate.DOORS);
        expected.put("minecraft:oak_fence_gate", IslandSuggestionTemplate.DOORS);
        // Beds → DECORATION (was functional_block). Decorated pots now
        // live in the Ceramics & Molds stock bucket.
        expected.put("minecraft:brown_bed", IslandSuggestionTemplate.DECORATION);
        expected.put("minecraft:red_bed", IslandSuggestionTemplate.DECORATION);
        expected.put("minecraft:decorated_pot", IslandSuggestionTemplate.CERAMICS_MOLDS);
        // Compressed material blocks → ORES_RAW_STOCK (the player's
        // "ore stockpile" island; they live with raw_iron / raw_copper /
        // raw_gold rather than the broader Materials catch-all).
        expected.put("minecraft:diamond_block", IslandSuggestionTemplate.ORES_RAW_STOCK);
        expected.put("minecraft:iron_block", IslandSuggestionTemplate.ORES_RAW_STOCK);
        expected.put("minecraft:gold_block", IslandSuggestionTemplate.ORES_RAW_STOCK);
        expected.put("minecraft:emerald_block", IslandSuggestionTemplate.ORES_RAW_STOCK);
        expected.put("minecraft:copper_block", IslandSuggestionTemplate.ORES_RAW_STOCK);
        // Rails → TRANSPORT (was functional_block / redstone_component).
        expected.put("minecraft:rail", IslandSuggestionTemplate.TRANSPORT);
        expected.put("minecraft:powered_rail", IslandSuggestionTemplate.TRANSPORT);
        expected.put("minecraft:detector_rail", IslandSuggestionTemplate.TRANSPORT);
        expected.put("minecraft:activator_rail", IslandSuggestionTemplate.TRANSPORT);
        // Spawn eggs → CURIOSITY (was utility / admin).
        expected.put("minecraft:bee_spawn_egg", IslandSuggestionTemplate.CURIOSITY);
        expected.put("minecraft:zombie_spawn_egg", IslandSuggestionTemplate.CURIOSITY);
        // Universal stock sections keep these out of the broad Materials pile.
        expected.put("minecraft:blaze_powder", IslandSuggestionTemplate.DUSTS_POWDERS);
        expected.put("minecraft:string", IslandSuggestionTemplate.ORGANIC_MATERIALS);
        expected.put("minecraft:leather", IslandSuggestionTemplate.ORGANIC_MATERIALS);
        expected.put("minecraft:feather", IslandSuggestionTemplate.ORGANIC_MATERIALS);
        expected.put("minecraft:bone", IslandSuggestionTemplate.ORGANIC_MATERIALS);
        expected.put("minecraft:slime_ball", IslandSuggestionTemplate.ORGANIC_MATERIALS);
        expected.put("minecraft:wheat_seeds", IslandSuggestionTemplate.SEEDS);
        expected.put("minecraft:wheat", IslandSuggestionTemplate.CROPS);
        expected.put("minecraft:carrot", IslandSuggestionTemplate.CROPS);
        expected.put("minecraft:oak_sapling", IslandSuggestionTemplate.PLANTS);
        expected.put("minecraft:clay_ball", IslandSuggestionTemplate.CERAMICS_MOLDS);
        expected.put("minecraft:brick", IslandSuggestionTemplate.CERAMICS_MOLDS);
        expected.put("minecraft:flower_pot", IslandSuggestionTemplate.CERAMICS_MOLDS);
        // Raw ores → ORES_RAW_STOCK, not the broad MATERIALS pile.
        expected.put("minecraft:raw_iron", IslandSuggestionTemplate.ORES_RAW_STOCK);

        FacetIndex index = FacetIndexBootstrap.loadVanillaBase();
        for (Map.Entry<String, IslandSuggestionTemplate> e : expected.entrySet()) {
            assertTrue(index.itemIds().contains(e.getKey()), "expected bundled vanilla entry for " + e.getKey());
            IslandSignalDescriptor descriptor = descriptorFrom(index, e.getKey());
            IslandSuggestionTemplate match = IslandSuggestionTemplate.firstMatch(descriptor);
            org.junit.jupiter.api.Assertions.assertEquals(e.getValue(), match,
                    e.getKey() + " (" + describe(descriptor) + ") should resolve to " + e.getValue());
        }
    }

    @Test
    void woodStockRoutesToWoodNotBroadNaturalOrMaterials() {
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
        // Wood stock is a first-order material bucket. It should not fall
        // into the broad Natural or Materials catch-alls.
        org.junit.jupiter.api.Assertions.assertEquals(IslandSuggestionTemplate.WOOD, match);
    }

    @Test
    void bundledPerModDataIsEmptyByDefault() {
        // Pack-specific classifications are now supplied by datapack/resource
        // layers under data/slot/classification/layers/*.json, not bundled into
        // the base mod jar for every player.
        FacetIndex index = FacetIndexBootstrap.loadAll();
        try (InputStream stream = FacetIndexBootstrap.class.getResourceAsStream(
                FacetIndexBootstrap.PER_MOD_INDEX_RESOURCE);
             Reader manifestReader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            JsonObject manifest = JsonParser.parseReader(manifestReader).getAsJsonObject();
            assertEquals(0, manifest.getAsJsonArray("mods").size());
        } catch (Exception e) {
            org.junit.jupiter.api.Assertions.fail("failed to read bundled per-mod manifest", e);
        }
        assertEquals(FacetIndexBootstrap.loadVanillaBase().size(), index.size());
    }

    private static IslandSignalDescriptor descriptorFrom(FacetIndex index, String itemId) {
        return new IslandSignalDescriptor(
                ItemIdentity.of(itemId),
                Set.of(),
                Set.of(),
                namespaceOf(itemId),
                "",
                index.role(itemId).orElse(null),
                index.roleAlternatives(itemId),
                index.materialFamily(itemId).orElse(null),
                index.subsystems(itemId),
                index.organizationGroups(itemId),
                index.activities(itemId),
                index.flavor(itemId).orElse(null),
                index.carryFrequency(itemId).orElse(null),
                index.rarity(itemId).orElse(null),
                index.origin(itemId).orElse(null),
                index.dyeColor(itemId).orElse(null),
                index.palette(itemId),
                index.form(itemId).orElse(null),
                index.emitsLight(itemId)
        );
    }

    private static String describe(IslandSignalDescriptor descriptor) {
        return "role=" + descriptor.role()
                + ", form=" + descriptor.form()
                + ", material_family=" + descriptor.materialFamily()
                + ", organization_groups=" + descriptor.organizationGroups()
                + ", activities=" + descriptor.activities();
    }

    private static String namespaceOf(String itemId) {
        int colon = itemId.indexOf(':');
        return colon <= 0 ? "" : itemId.substring(0, colon);
    }
}
