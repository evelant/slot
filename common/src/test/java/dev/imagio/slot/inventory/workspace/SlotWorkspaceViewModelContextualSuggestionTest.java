package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.classification.FacetIndex;
import dev.imagio.slot.classification.FacetIndexHolder;
import dev.imagio.slot.inventory.core.BuiltinInventoryDescriptors;
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.HostInstanceKey;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventoryStackSnapshot;
import dev.imagio.slot.inventory.core.InventoryTopologyDescriptor;
import dev.imagio.slot.inventory.core.PlayerRuntimeStateDescriptor;
import dev.imagio.slot.inventory.integration.InventoryHostObservationHints;
import dev.imagio.slot.inventory.integration.InventoryHostSession;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.testsupport.InventoryAuthorityFixtures;
import dev.imagio.slot.workflow.domain.WorkflowDomainSnapshot;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlotWorkspaceViewModelContextualSuggestionTest {
    @Test
    void putAwayDoesNotScoreUnhomedCarriedItemsFromCarryFrequencyAlone() {
        FacetIndexHolder.install(FacetIndex.load(new StringReader("""
                {
                  "schema_version": 1,
                  "layer": "server",
                  "entries": {
                    "minecraft:flower_pot": {
                      "facets": {
                        "workflow": {"values": ["decoration"]},
                        "workflow_role": {"values": ["display"]},
                        "carry_frequency": {"value": "display_only"}
                      }
                    }
                  }
                }
                """)));
        try {
            SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.project(
                    carried("minecraft:flower_pot", 1),
                    WorkflowDomainSnapshot.empty(),
                    "ready",
                    "",
                    0,
                    0,
                    1L);

            assertTrue(viewModel.triageItems().stream()
                    .anyMatch(item -> item.identity().itemId().equals("minecraft:flower_pot")));
            assertFalse(viewModel.contextualSuggestionLanes().stream()
                    .filter(lane -> SlotWorkspaceViewModel.ContextualSuggestionLane.PUT_AWAY.equals(lane.id()))
                    .flatMap(lane -> lane.items().stream())
                    .anyMatch(item -> item.identity().itemId().equals("minecraft:flower_pot")));
        } finally {
            FacetIndexHolder.reset();
        }
    }

    private static InventoryAuthoritySnapshot carried(String itemId, int count) {
        return InventoryAuthorityFixtures.authority(
                host(),
                Map.of(BuiltinInventoryIds.PLAYER_MAIN,
                        List.of(new InventoryStackSnapshot(0, new ItemStack(itemId, count, 64), count))),
                Map.of());
    }

    private static InventoryHostDescriptor host() {
        TestMenu menu = new TestMenu();
        return new InventoryHostDescriptor(
                new HostInstanceKey(TestMenu.class.getName(), 0, "slot.workspace.contextual.test", ""),
                InventoryHostDescriptor.serverMenuRef(menu),
                "slot.workspace.contextual.test",
                Component.literal("Workspace Contextual Test"),
                menu,
                InventoryTopologyDescriptor.empty(),
                InventoryHostSession.empty(),
                List.of(),
                PlayerRuntimeStateDescriptor.vanilla(0),
                BuiltinInventoryDescriptors.builtInPlayerSources(InventoryTopologyDescriptor.empty()),
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
