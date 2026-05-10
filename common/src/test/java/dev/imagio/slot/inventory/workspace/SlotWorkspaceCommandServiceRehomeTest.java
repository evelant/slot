package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.classification.FacetIndex;
import dev.imagio.slot.classification.FacetIndexHolder;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.triage.IslandSignalDescriptor;
import dev.imagio.slot.inventory.triage.IslandTemplateMatch;
import dev.imagio.slot.workflow.domain.DomainEventMetadata;
import dev.imagio.slot.workflow.domain.InMemoryWorkflowDomainStateRepository;
import dev.imagio.slot.workflow.domain.VisualHomeAssignment;
import dev.imagio.slot.workflow.domain.VisualHomeOrigin;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SlotWorkspaceCommandServiceRehomeTest {

    @Test
    void reclassifyHomesMaterializesQualifiedSubsystemAndReassignsIdentity() {
        FacetIndexHolder.install(FacetIndex.load(new StringReader(layerWithCastingCohort())));
        try {
            WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(
                    new InMemoryWorkflowDomainStateRepository(),
                    null
            );
            ItemIdentity stick = ItemIdentity.of("minecraft:stick");
            runtime.visualAtlasWorkflow().createIslandWithId(
                    "old.materials",
                    "Materials",
                    0,
                    0,
                    0x777777,
                    stick,
                    DomainEventMetadata.origin("test.old_island")
            );
            runtime.visualAtlasWorkflow().assignHome(
                    stick,
                    "old.materials",
                    0,
                    VisualHomeOrigin.PLAYER_PLACED,
                    true,
                    DomainEventMetadata.origin("test.old_home")
            );

            SlotWorkspaceCommandService.ClassificationRehomeResult result =
                    SlotWorkspaceCommandService.reclassifyHomes(
                            runtime,
                            List.of(new ItemStack("minecraft:stick", 1, 64)),
                            stack -> castingDescriptor(stick)
                    );

            assertEquals(1, result.inputStacks());
            assertEquals(1, result.uniqueIdentities());
            assertEquals(1, result.assigned());
            assertEquals(1, result.changed());
            assertEquals(0, result.unchanged());
            assertEquals(1, result.islandsCreated());
            VisualHomeAssignment assignment = runtime.visualAtlasWorkflow().visualHomeMap().assignment(stick);
            assertNotNull(assignment);
            assertEquals(IslandTemplateMatch.SUBSYSTEM_ISLAND_PREFIX + "test:casting", assignment.islandId());
            assertEquals(VisualHomeOrigin.AUTO_HOMED, assignment.origin());
        } finally {
            FacetIndexHolder.reset();
        }
    }

    private static IslandSignalDescriptor castingDescriptor(ItemIdentity identity) {
        return new IslandSignalDescriptor(
                identity,
                Set.of(),
                Set.of(),
                "minecraft",
                "",
                "utility",
                List.of("utility"),
                null,
                List.of("test:casting"),
                List.of(),
                null,
                null,
                null,
                null,
                null,
                List.of(),
                null,
                false
        );
    }

    private static String layerWithCastingCohort() {
        return """
                {
                  "schema_version": 1,
                  "layer": "modpack",
                  "entries": {
                    "minecraft:stick": {"facets": {"role": {"value": "utility"}, "mod_subsystem": {"values": ["test:casting"]}}},
                """
                + cohortEntries()
                + """

                  }
                }
                """;
    }

    private static String cohortEntries() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            builder.append("    \"test:casting_tool_")
                    .append(i)
                    .append("\": {\"facets\": {\"role\": {\"value\": \"utility\"}, \"mod_subsystem\": {\"values\": [\"test:casting\"]}}}");
            if (i < 8) {
                builder.append(',');
            }
            builder.append('\n');
        }
        return builder.toString();
    }
}
