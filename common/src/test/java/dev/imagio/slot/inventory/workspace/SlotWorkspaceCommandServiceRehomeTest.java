package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.classification.FacetIndex;
import dev.imagio.slot.classification.FacetIndexHolder;
import dev.imagio.slot.inventory.core.ItemComparisonMode;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.query.CursorStateSnapshot;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.triage.IslandSignalDescriptor;
import dev.imagio.slot.inventory.triage.LearnedIslandRuleStore;
import dev.imagio.slot.workflow.domain.DomainEventMetadata;
import dev.imagio.slot.workflow.domain.InMemoryWorkflowDomainStateRepository;
import dev.imagio.slot.workflow.domain.VisualHomeAssignment;
import dev.imagio.slot.workflow.domain.VisualHomeOrigin;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlotWorkspaceCommandServiceRehomeTest {

    @Test
    void assignHomeAllowsIdentityCurrentlyOnMenuCursor() {
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(
                new InMemoryWorkflowDomainStateRepository(),
                null
        );
        ItemIdentity apple = ItemIdentity.of("minecraft:apple");
        runtime.visualAtlasWorkflow().createIslandWithId(
                "food",
                "Food",
                0,
                0,
                0xAA8844,
                apple,
                DomainEventMetadata.origin("test.food"));
        runtime.visualAtlasWorkflow().createIslandWithId(
                "building",
                "Building",
                0,
                0,
                0x6688AA,
                null,
                DomainEventMetadata.origin("test.building"));
        runtime.visualAtlasWorkflow().assignHome(
                apple,
                "food",
                0,
                VisualHomeOrigin.PLAYER_PLACED,
                true,
                DomainEventMetadata.origin("test.old_home"));

        InventoryAuthoritySnapshot authority = new InventoryAuthoritySnapshot(
                null,
                Map.of(),
                new CursorStateSnapshot(new ItemStack("minecraft:apple", 1, 64), ""));
        SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.project(
                authority,
                runtime.snapshot(),
                "ready",
                "",
                0,
                0,
                1);
        assertNull(viewModel.atlasItem(SlotWorkspaceViewModel.IdentityRef.from(apple)));

        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.assignHome(
                runtime,
                viewModel,
                new LearnedIslandRuleStore(),
                stack -> null,
                authority,
                apple.itemId(),
                ItemComparisonMode.ITEM_ID.name(),
                "",
                "building",
                null);

        assertTrue(outcome.success());
        VisualHomeAssignment assignment = runtime.visualAtlasWorkflow().visualHomeMap().assignment(apple);
        assertNotNull(assignment);
        assertEquals("building", assignment.islandId());
    }

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
    void reclassifyHomesIgnoresOrganizationGroupWhileGroupHomingDisabled() {
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
            assertEquals("materials", assignment.islandId());
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
