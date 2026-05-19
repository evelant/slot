package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorkflowAcceptedInputOptionsTest {
    @Test
    void optionsIncludeExactAndRankMaterialSpecificTagsBeforeBroadTags() {
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
        assertEquals(WorkflowAcceptedInputRule.itemTag("c:ores"), options.get(2));
        assertEquals(WorkflowAcceptedInputRule.itemTag("tfc:metal_ores"), options.get(3));
        assertEquals(WorkflowAcceptedInputRule.itemTag("tfc:ore_pieces"), options.get(4));
        assertEquals(5, options.size());
    }

    @Test
    void optionsCapTagChoicesButKeepBroadOreGroupWithinMenu() {
        List<WorkflowAcceptedInputRule> options = WorkflowAcceptedInputOptions.forItem(
                ItemIdentity.of("gtceu:crushed_hematite_ore"),
                List.of(
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
    }
}
