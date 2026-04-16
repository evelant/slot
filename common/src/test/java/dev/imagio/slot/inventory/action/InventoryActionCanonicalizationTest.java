package dev.imagio.slot.inventory.action;

import dev.imagio.slot.inventory.core.HostInstanceKey;
import dev.imagio.slot.inventory.core.InventoryActionRoute;
import dev.imagio.slot.inventory.core.InventoryBindingRoute;
import dev.imagio.slot.inventory.core.InventoryCapability;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventoryPaneMembership;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.InventorySourceDomain;
import dev.imagio.slot.inventory.core.InventorySourceRole;
import dev.imagio.slot.inventory.core.InventoryToolDescriptor;
import dev.imagio.slot.inventory.core.InventoryToolKind;
import dev.imagio.slot.inventory.core.InventoryTopologyDescriptor;
import dev.imagio.slot.inventory.core.PlayerRuntimeStateDescriptor;
import dev.imagio.slot.inventory.core.ToolPresentationHints;
import dev.imagio.slot.inventory.core.ToolRegionDescriptor;
import dev.imagio.slot.inventory.core.ToolRegionRole;
import dev.imagio.slot.inventory.integration.InventoryHostSession;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InventoryActionCanonicalizationTest {
    @Test
    void hostAwareCanonicalizationTreatsToolRegionsAsLinkedSourceSlots() {
        InventoryHostDescriptor host = host();
        InventoryActionRequest request = new InventoryActionRequest(
                host.hostId(),
                host.serverMenuRef(),
                "request-1",
                InventoryActionKind.TRANSFER,
                InventoryActionMode.EXECUTE,
                InventoryActionQuantity.STACK,
                InventoryActionScope.SINGLE_TARGET,
                InventoryActionConflictPolicy.INSERT_ONLY,
                "test",
                new InventoryActionTarget.ToolRegionTarget("crafting.tool", "crafting/input", 0),
                null,
                0,
                null,
                ItemStack.EMPTY,
                dev.imagio.slot.inventory.core.InventoryToolActionId.PROVIDER_DEFINED,
                dev.imagio.slot.inventory.core.InventoryToolToggleId.PROVIDER_DEFINED,
                false,
                ""
        );

        assertEquals(
                "source:tool.input.source#0",
                InventoryTargetCanonicalizer.canonicalKey(host, request.primaryTarget())
        );

        InventoryActionOutcome outcome = new InventoryActionOutcome(
                host.hostId(),
                host.serverMenuRef(),
                "request-1",
                InventoryActionKind.TRANSFER,
                InventoryActionMode.EXECUTE,
                InventoryActionQuantity.STACK,
                InventoryActionScope.SINGLE_TARGET,
                InventoryActionConflictPolicy.INSERT_ONLY,
                "test",
                request.primaryTarget(),
                null,
                true,
                List.of(),
                ItemStack.EMPTY,
                ""
        );
        assertEquals(Set.of("source:tool.input.source#0"), outcome.targetKeys(host));
    }

    @Test
    void sourceTargetsRemainDistinctFromExactSlotAndEntryTargets() {
        InventoryHostDescriptor host = host();
        InventoryActionRequest request = new InventoryActionRequest(
                host.hostId(),
                host.serverMenuRef(),
                "request-2",
                InventoryActionKind.TRANSFER,
                InventoryActionMode.EXECUTE,
                InventoryActionQuantity.STACK,
                InventoryActionScope.SINGLE_TARGET,
                InventoryActionConflictPolicy.INSERT_ONLY,
                "test",
                new InventoryActionTarget.SourceSlotTarget("tool.input.source", 0),
                new InventoryActionTarget.SourceTarget("tool.input.source"),
                1,
                null,
                ItemStack.EMPTY,
                dev.imagio.slot.inventory.core.InventoryToolActionId.PROVIDER_DEFINED,
                dev.imagio.slot.inventory.core.InventoryToolToggleId.PROVIDER_DEFINED,
                false,
                ""
        );

        assertEquals("source:tool.input.source", InventoryTargetCanonicalizer.canonicalKey(host, new InventoryActionTarget.SourceTarget("tool.input.source")));
        assertEquals("source:tool.input.source#0", InventoryTargetCanonicalizer.canonicalKey(host, new InventoryActionTarget.SourceSlotTarget("tool.input.source", 0)));
        assertEquals("entry:tool.input.source@entry-1", InventoryTargetCanonicalizer.canonicalKey(host, new InventoryActionTarget.SourceEntryTarget("tool.input.source", "entry-1")));

        InventoryActionOutcome outcome = new InventoryActionOutcome(
                host.hostId(),
                host.serverMenuRef(),
                "request-2",
                InventoryActionKind.TRANSFER,
                InventoryActionMode.EXECUTE,
                InventoryActionQuantity.STACK,
                InventoryActionScope.SINGLE_TARGET,
                InventoryActionConflictPolicy.INSERT_ONLY,
                "test",
                request.primaryTarget(),
                request.secondaryTarget(),
                true,
                List.of(),
                ItemStack.EMPTY,
                ""
        );
        assertEquals(Set.of("source:tool.input.source#0", "source:tool.input.source"), outcome.targetKeys(host));
    }

    private static InventoryHostDescriptor host() {
        TestMenu menu = new TestMenu();
        InventorySourceDescriptor source = InventorySourceDescriptor.builder("tool.input.source")
                .label(Component.literal("Tool Input"))
                .domain(InventorySourceDomain.TOOL_REGION)
                .role(InventorySourceRole.PROVIDER_DEFINED)
                .logicalSlotCount(1)
                .bindingRoute(InventoryBindingRoute.MENU)
                .capabilities(Set.of(InventoryCapability.INSERT, InventoryCapability.EXTRACT))
                .actionRoute(InventoryActionRoute.MENU_MUTATION)
                .paneMembership(InventoryPaneMembership.HIDDEN)
                .stableOrder(100)
                .build();
        InventoryToolDescriptor tool = new InventoryToolDescriptor(
                "crafting.tool",
                "test.provider",
                InventoryToolKind.CRAFTING_GRID,
                Component.literal("Crafting"),
                new ToolPresentationHints("Crafting", 70, "docked", 70),
                70,
                true,
                true,
                null,
                List.of(new ToolRegionDescriptor(
                        "crafting/input",
                        ToolRegionRole.INPUT,
                        1,
                        InventoryBindingRoute.MENU,
                        Set.of(InventoryCapability.INSERT, InventoryCapability.EXTRACT, InventoryCapability.TOOL_REGION_MUTATION),
                        true,
                        "tool.input.source",
                        ""
                )),
                List.of(),
                List.of(),
                Map.of(),
                Map.of(),
                ""
        );
        return new InventoryHostDescriptor(
                new HostInstanceKey(TestMenu.class.getName(), 1, "test.provider", ""),
                InventoryHostDescriptor.serverMenuRef(menu),
                "test.screen",
                Component.literal("Test"),
                menu,
                InventoryTopologyDescriptor.empty(),
                InventoryHostSession.empty(),
                List.of(),
                PlayerRuntimeStateDescriptor.vanilla(0),
                List.of(source),
                List.of(),
                List.of(),
                List.of(tool),
                dev.imagio.slot.inventory.integration.InventoryHostObservationHints.defaults(),
                ""
        );
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
