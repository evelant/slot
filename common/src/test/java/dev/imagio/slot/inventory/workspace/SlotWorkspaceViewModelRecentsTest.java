package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.BuiltinInventoryDescriptors;
import dev.imagio.slot.inventory.core.HostInstanceKey;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventoryPaneMembership;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.InventorySourceDomain;
import dev.imagio.slot.inventory.core.InventorySourceRole;
import dev.imagio.slot.inventory.core.InventoryStackSnapshot;
import dev.imagio.slot.inventory.core.InventoryTopologyDescriptor;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.PlayerRuntimeStateDescriptor;
import dev.imagio.slot.inventory.integration.InventoryHostObservationHints;
import dev.imagio.slot.inventory.integration.InventoryHostSession;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.testsupport.InventoryAuthorityFixtures;
import dev.imagio.slot.workflow.domain.InMemoryWorkflowDomainStateRepository;
import dev.imagio.slot.workflow.domain.InventoryActivityConfidence;
import dev.imagio.slot.workflow.domain.InventoryActivityEvent;
import dev.imagio.slot.workflow.domain.InventoryActivityKind;
import dev.imagio.slot.workflow.domain.InventoryActivityProducer;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlotWorkspaceViewModelRecentsTest {
    private static final String CRAFTING_INPUT = "test.crafting/input";

    @Test
    void recentIdentityStaysRenderableWhenOnlyStackIsInHiddenCraftingInput() {
        WorkflowDomainRuntime runtime = new WorkflowDomainRuntime(new InMemoryWorkflowDomainStateRepository(), null);
        ItemIdentity planks = ItemIdentity.of("minecraft:oak_planks");
        runtime.recordActivityEvent(acquired(planks, 4));

        SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.project(
                authorityWithCraftingInput("minecraft:oak_planks", 4),
                runtime.snapshot(),
                "ready",
                "",
                0,
                0,
                1L);

        SlotWorkspaceViewModel.IdentityRef ref = SlotWorkspaceViewModel.IdentityRef.from(planks);
        SlotWorkspaceViewModel.AtlasItem item = viewModel.atlasItem(ref);

        assertEquals(List.of(ref), viewModel.recentIdentities());
        assertNotNull(item);
        assertTrue(item.recent());
        assertTrue(item.ghost());
        assertFalse(item.carried());
        assertFalse(item.displayStack().isEmpty());
    }

    private static InventoryActivityEvent acquired(ItemIdentity identity, int count) {
        return new InventoryActivityEvent(
                InventoryActivityKind.ACQUIRED,
                InventoryActivityProducer.WORLD_PICKUP,
                InventoryActivityConfidence.AUTHORITATIVE,
                identity,
                count,
                null,
                null,
                "",
                "",
                List.of(),
                "test");
    }

    private static InventoryAuthoritySnapshot authorityWithCraftingInput(
            String itemId,
            int count
    ) {
        return InventoryAuthorityFixtures.authority(
                host(),
                Map.of(CRAFTING_INPUT, List.of(new InventoryStackSnapshot(
                        0,
                        new ItemStack(itemId, count, 64),
                        count))),
                Map.of(CRAFTING_INPUT, 4));
    }

    private static InventoryHostDescriptor host() {
        TestMenu menu = new TestMenu();
        InventoryTopologyDescriptor topology = InventoryTopologyDescriptor.empty();
        ArrayList<InventorySourceDescriptor> sources = new ArrayList<>(
                BuiltinInventoryDescriptors.builtInPlayerSources(topology));
        sources.add(InventorySourceDescriptor.builder(CRAFTING_INPUT)
                .label(Component.literal("Crafting Input"))
                .domain(InventorySourceDomain.TOOL_REGION)
                .role(InventorySourceRole.PROVIDER_DEFINED)
                .logicalSlotCount(4)
                .paneMembership(InventoryPaneMembership.HIDDEN)
                .stableOrder(200)
                .build());
        return new InventoryHostDescriptor(
                new HostInstanceKey(TestMenu.class.getName(), 0, "slot.workspace.recents.test", ""),
                InventoryHostDescriptor.serverMenuRef(menu),
                "slot.workspace.recents.test",
                Component.literal("Workspace Recents Test"),
                menu,
                topology,
                InventoryHostSession.empty(),
                List.of(),
                PlayerRuntimeStateDescriptor.vanilla(0),
                sources,
                BuiltinInventoryDescriptors.builtInQuickAccessLanes(),
                BuiltinInventoryDescriptors.builtInEquipmentGroups(),
                List.of(),
                InventoryHostObservationHints.defaults(),
                "");
    }

    private static final class TestMenu extends AbstractContainerMenu {
        private TestMenu() {
            super(null, 0);
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }
    }
}
