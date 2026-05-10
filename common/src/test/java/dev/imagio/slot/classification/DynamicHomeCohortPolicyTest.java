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
    void qualifiesOnlyBroadAllowedSubsystemCohortsAtThreshold() {
        ArrayList<String> entries = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            entries.add(entry("tfc:ceramic/casting_mold_" + i, "utility", "tfc:casting"));
            entries.add(entry("tfc:fiber/thread_" + i, "material", "tfc:weaving"));
            entries.add(entry("create:decorative_panel_" + i, "decorative_block", "create:decoration"));
        }
        for (int i = 0; i < 9; i++) {
            entries.add(entry("tfc:tiny_tool_" + i, "utility", "tfc:tiny_mechanic"));
        }

        DynamicHomeCohortPolicy policy = DynamicHomeCohortPolicy.from(
                FacetIndex.load(new StringReader(layer(entries))));

        assertTrue(policy.qualifies("tfc:casting"));
        assertTrue(policy.qualifies("tfc:weaving"));
        assertEquals(10, policy.count("tfc:casting"));
        assertEquals(10, policy.count("tfc:weaving"));
        assertFalse(policy.qualifies("tfc:tiny_mechanic"));
        assertFalse(policy.qualifies("create:decoration"));
        assertEquals(0, policy.count("create:decoration"));
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
}
