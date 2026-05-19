package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class WorkflowAcceptedInputOptionsTest {
    @Test
    void optionsIncludeExactAndSkipBroadProcessTags() {
        List<WorkflowAcceptedInputRule> options = WorkflowAcceptedInputOptions.forItem(
                ItemIdentity.of("tfc:ore/normal_hematite/granite"),
                Set.of(
                        "tfc:metal_ores",
                        "c:ores",
                        "c:ores/cast_iron/normal",
                        "tfc:ore_pieces",
                        "slot_without_namespace"
                ));

        assertEquals(WorkflowAcceptedInputRule.exact(ItemIdentity.of("tfc:ore/normal_hematite/granite")), options.get(0));
        assertEquals(WorkflowAcceptedInputRule.itemTag("c:ores/cast_iron/normal"), options.get(1));
        assertEquals(2, options.size());
    }

    @Test
    void optionsCapTagChoicesAfterSkippingBroadProcessTags() {
        List<WorkflowAcceptedInputRule> options = WorkflowAcceptedInputOptions.forItem(
                ItemIdentity.of("gtceu:crushed_hematite_ore"),
                List.of(
                        "forge:crushed_ores",
                        "forge:dusts",
                        "minecraft:ignored_0",
                        "minecraft:ignored_1",
                        "minecraft:ignored_2",
                        "minecraft:ignored_3",
                        "minecraft:ignored_4",
                        "minecraft:ignored_5",
                        "minecraft:ignored_6",
                        "minecraft:ignored_7",
                        "c:ores/cast_iron/normal",
                        "c:dusts/iron"
                ));

        assertEquals(9, options.size());
        assertEquals(WorkflowAcceptedInputRule.itemTag("c:dusts/iron"), options.get(1));
        assertEquals(WorkflowAcceptedInputRule.itemTag("c:ores/cast_iron/normal"), options.get(2));
        assertFalse(options.contains(WorkflowAcceptedInputRule.itemTag("forge:crushed_ores")));
        assertFalse(options.contains(WorkflowAcceptedInputRule.itemTag("forge:dusts")));
    }
}
