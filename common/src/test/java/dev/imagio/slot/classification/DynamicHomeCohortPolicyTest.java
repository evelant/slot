package dev.imagio.slot.classification;

import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DynamicHomeCohortPolicyTest {

    @Test
    void countsAndQualifiesOrganizationGroupCohorts() {
        ArrayList<String> entries = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            entries.add(entry("tfc:ceramic/casting_mold_" + i, "utility", "tfc:casting"));
            entries.add(entry("tfc:fiber/thread_" + i, "material", "tfc:weaving"));
            entries.add(entry("create:cogwheel_" + i, "mechanism", "create:mechanical_power"));
            entries.add(entry("modded:redstone_link_" + i, "redstone_component", "modded:redstone_network"));
            entries.add(entry("create:decorative_panel_" + i, "decorative_block", "create:decoration"));
            entries.add(groupEntry("tfc:brick/masonry_" + i, "material", "tfc:masonry"));
            entries.add(groupEntry("tfc:decorative/masonry_" + i, "decorative_block", "tfc:masonry_decor"));
        }
        for (int i = 0; i < 9; i++) {
            entries.add(entry("tfc:tiny_tool_" + i, "utility", "tfc:tiny_mechanic"));
            entries.add(groupEntry("tfc:tiny_group_" + i, "material", "tfc:tiny_group"));
        }

        DynamicHomeCohortPolicy policy = DynamicHomeCohortPolicy.from(
                FacetIndex.load(new StringReader(layer(entries))));

        assertFalse(policy.qualifies("tfc:casting"));
        assertFalse(policy.qualifies("tfc:weaving"));
        assertEquals(0, policy.count("tfc:casting"));
        assertEquals(0, policy.count("tfc:weaving"));
        assertFalse(policy.qualifies("create:mechanical_power"));
        assertFalse(policy.qualifies("modded:redstone_network"));
        assertEquals(0, policy.count("create:mechanical_power"));
        assertEquals(0, policy.count("modded:redstone_network"));
        assertFalse(policy.qualifies("tfc:tiny_mechanic"));
        assertFalse(policy.qualifies("create:decoration"));
        assertEquals(0, policy.count("create:decoration"));
        assertEquals(10, policy.organizationGroupCount("tfc:masonry"));
        assertEquals(10, policy.organizationGroupCount("tfc:masonry_decor"));
        assertTrue(policy.organizationGroupQualifies("tfc:masonry"));
        assertTrue(policy.organizationGroupQualifies("tfc:masonry_decor"));
        assertFalse(policy.organizationGroupQualifies("tfc:tiny_group"));
    }

    @Test
    void bundledVanillaGroupsCanQualifyForDynamicHomes() {
        DynamicHomeCohortPolicy policy = DynamicHomeCohortPolicy.from(FacetIndexBootstrap.loadVanillaBase());

        assertTrue(policy.organizationGroupCount("building_blocks") >= DynamicHomeCohortPolicy.DEFAULT_MIN_SUBSYSTEM_ITEMS);
        assertTrue(policy.organizationGroupQualifies("building_blocks"));
    }

    private static String layer(List<String> entries) {
        return """
                {
                  "schema_version": 1,
                  "layer": "modpack",
                  "entries": {
                """
                + String.join(",\n", entries)
                + """

                  }
                }
                """;
    }

    private static String entry(String itemId, String role, String subsystem) {
        return "    \"" + itemId + "\": {\"facets\": {"
                + "\"role\": {\"value\": \"" + role + "\"}, "
                + "\"mod_subsystem\": {\"values\": [\"" + subsystem + "\"]}"
                + "}}";
    }

    private static String groupEntry(String itemId, String role, String group) {
        return "    \"" + itemId + "\": {\"facets\": {"
                + "\"role\": {\"value\": \"" + role + "\"}, "
                + "\"organization_group\": {\"values\": [\"" + group + "\"]}"
                + "}}";
    }
}
