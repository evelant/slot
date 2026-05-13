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
    void reclassifyHomesIgnoresSubsystemSectionsAndReassignsToParentTemplate() {
        FacetIndexHolder.install(FacetIndex.load(new StringReader(layerWithCastingCohort())));
        try {
            WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(
                    new InMemoryWorkflowDomainStateRepository(),
                    null
            );
            ItemIdentity cogwheel = ItemIdentity.of("create:cogwheel");
            runtime.visualAtlasWorkflow().createIslandWithId(
                    "old.materials",
                    "Materials",
                    0,
                    0,
                    0x777777,
                    cogwheel,
                    DomainEventMetadata.origin("test.old_island")
            );
            runtime.visualAtlasWorkflow().assignHome(
                    cogwheel,
                    "old.materials",
                    0,
                    VisualHomeOrigin.PLAYER_PLACED,
                    true,
                    DomainEventMetadata.origin("test.old_home")
            );

            SlotWorkspaceCommandService.ClassificationRehomeResult result =
                    SlotWorkspaceCommandService.reclassifyHomes(
                            runtime,
                            List.of(new ItemStack("create:cogwheel", 1, 64)),
                            stack -> castingDescriptor(cogwheel)
                    );

            assertEquals(1, result.inputStacks());
            assertEquals(1, result.uniqueIdentities());
            assertEquals(1, result.assigned());
            assertEquals(1, result.changed());
            assertEquals(0, result.unchanged());
            assertEquals(1, result.islandsCreated());
            VisualHomeAssignment assignment = runtime.visualAtlasWorkflow().visualHomeMap().assignment(cogwheel);
            assertNotNull(assignment);
            assertEquals("mechanisms", assignment.islandId());
            assertEquals(VisualHomeOrigin.AUTO_HOMED, assignment.origin());
        } finally {
            FacetIndexHolder.reset();
        }
    }

    @Test
    void reclassifyHomesPrefersQualifiedOrganizationGroup() {
        FacetIndexHolder.install(FacetIndex.load(new StringReader(layerWithMasonryGroupCohort())));
        try {
            WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(
                    new InMemoryWorkflowDomainStateRepository(),
                    null
            );
            ItemIdentity mortar = ItemIdentity.of("tfc:mortar");

            SlotWorkspaceCommandService.ClassificationRehomeResult result =
                    SlotWorkspaceCommandService.reclassifyHomes(
                            runtime,
                            List.of(new ItemStack("tfc:mortar", 1, 64)),
                            stack -> masonryDescriptor(mortar)
                    );

            assertEquals(1, result.assigned());
            assertEquals(1, result.islandsCreated());
            VisualHomeAssignment assignment = runtime.visualAtlasWorkflow().visualHomeMap().assignment(mortar);
            assertNotNull(assignment);
            assertEquals(IslandTemplateMatch.ORGANIZATION_GROUP_ISLAND_PREFIX + "tfc:masonry", assignment.islandId());
            assertEquals(VisualHomeOrigin.AUTO_HOMED, assignment.origin());
        } finally {
            FacetIndexHolder.reset();
        }
    }

    @Test
    void reclassifyHomesDoesNotRewriteUnchangedAssignments() {
        FacetIndexHolder.install(FacetIndex.load(new StringReader(layerWithMasonryGroupCohort())));
        try {
            InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
            WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(repository, null);
            ItemIdentity mortar = ItemIdentity.of("tfc:mortar");
            List<ItemStack> stacks = List.of(new ItemStack("tfc:mortar", 1, 64));

            SlotWorkspaceCommandService.reclassifyHomes(runtime, stacks, stack -> masonryDescriptor(mortar));
            int eventsAfterFirstRun = repository.workflowEvents().snapshot().records().size();

            SlotWorkspaceCommandService.ClassificationRehomeResult second =
                    SlotWorkspaceCommandService.reclassifyHomes(runtime, stacks, stack -> masonryDescriptor(mortar));

            assertEquals(1, second.assigned());
            assertEquals(0, second.changed());
            assertEquals(1, second.unchanged());
            assertEquals(eventsAfterFirstRun, repository.workflowEvents().snapshot().records().size());
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
                "mechanism",
                List.of("mechanism"),
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

    private static IslandSignalDescriptor masonryDescriptor(ItemIdentity identity) {
        return new IslandSignalDescriptor(
                identity,
                Set.of(),
                Set.of(),
                "tfc",
                "",
                "material",
                List.of("material"),
                null,
                List.of("tfc:casting"),
                List.of("tfc:masonry"),
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
                    "create:cogwheel": {"facets": {"role": {"value": "mechanism"}, "mod_subsystem": {"values": ["test:casting"]}}},
                """
                + cohortEntries()
                + """

                  }
                }
                """;
    }

    private static String layerWithMasonryGroupCohort() {
        return """
                {
                  "schema_version": 1,
                  "layer": "modpack",
                  "entries": {
                    "tfc:mortar": {"facets": {"role": {"value": "material"}, "organization_group": {"values": ["tfc:masonry"]}, "mod_subsystem": {"values": ["tfc:casting"]}}},
                """
                + groupCohortEntries()
                + """

                  }
                }
                """;
    }

    private static String groupCohortEntries() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            builder.append("    \"tfc:masonry_")
                    .append(i)
                    .append("\": {\"facets\": {\"role\": {\"value\": \"material\"}, \"organization_group\": {\"values\": [\"tfc:masonry\"]}}}");
            if (i < 8) {
                builder.append(',');
            }
            builder.append('\n');
        }
        return builder.toString();
    }

    private static String cohortEntries() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            builder.append("    \"test:casting_tool_")
                    .append(i)
                    .append("\": {\"facets\": {\"role\": {\"value\": \"mechanism\"}, \"mod_subsystem\": {\"values\": [\"test:casting\"]}}}");
            if (i < 8) {
                builder.append(',');
            }
            builder.append('\n');
        }
        return builder.toString();
    }
}
