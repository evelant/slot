package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.InventoryActionRoute;
import dev.imagio.slot.inventory.core.InventoryBindingRoute;
import dev.imagio.slot.inventory.core.InventoryCapability;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventoryPaneMembership;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.InventorySourceDomain;
import dev.imagio.slot.inventory.core.InventorySourceRole;
import dev.imagio.slot.inventory.integration.InventoryHostSession;
import dev.imagio.slot.inventory.integration.InventoryIntegrationProvider;
import dev.imagio.slot.inventory.integration.InventoryIntegrationRegistry;
import dev.imagio.slot.inventory.integration.InventoryMutationMode;
import dev.imagio.slot.inventory.integration.InventoryMutationRequest;
import dev.imagio.slot.inventory.integration.MutationResult;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.registry.ProviderResult;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkspaceCursorCommandServiceTest {
    @AfterEach
    void clearIntegrationRegistry() {
        InventoryIntegrationRegistry.clear();
    }

    @Test
    void chestPickupInvalidationUsesStorageAndIdentityRecords() {
        ItemIdentity stone = ItemIdentity.of("minecraft:stone");
        String storageId = "00000000-0000-0000-0000-000000000101";

        List<WorkspaceInvalidation> invalidations = WorkspaceCursorCommandService.pickupInvalidations(
                stone,
                new WorkspaceCursorCommandService.CursorOrigin(
                        WorkspaceCursorCommandService.CursorSourceKind.CHEST,
                        storageId,
                        2),
                4);

        assertEquals(1, invalidations.size());
        WorkspaceInvalidation invalidation = invalidations.get(0);
        assertEquals(WorkspaceInvalidation.Reason.MENU_SLOT_CHANGED, invalidation.reason());
        assertFalse(invalidation.requiresFullProjection());
        assertEquals(Set.of(stone), invalidation.identities());
        assertEquals(Set.of(storageId), invalidation.storageIds());
        assertTrue(invalidation.slices().contains(WorkspaceProjectionSlice.CARD));
        assertTrue(invalidation.slices().contains(WorkspaceProjectionSlice.STORAGE));
        assertTrue(invalidation.slices().contains(WorkspaceProjectionSlice.WAYFINDING));
        assertEquals("cursor_pickup_chest", invalidation.diagnostics());
    }

    @Test
    void carriedPickupInvalidationUsesCarriedIdentity() {
        ItemIdentity stone = ItemIdentity.of("minecraft:stone");

        List<WorkspaceInvalidation> invalidations = WorkspaceCursorCommandService.pickupInvalidations(
                stone,
                new WorkspaceCursorCommandService.CursorOrigin(
                        WorkspaceCursorCommandService.CursorSourceKind.CARRY,
                        "PLAYER_MAIN",
                        4),
                8);

        assertEquals(1, invalidations.size());
        WorkspaceInvalidation invalidation = invalidations.get(0);
        assertEquals(WorkspaceInvalidation.Reason.CARRIED_REVISION_CHANGED, invalidation.reason());
        assertFalse(invalidation.requiresFullProjection());
        assertEquals(Set.of(stone), invalidation.identities());
        assertEquals(Set.of(), invalidation.storageIds());
        assertTrue(invalidation.slices().contains(WorkspaceProjectionSlice.CARD));
        assertTrue(invalidation.slices().contains(WorkspaceProjectionSlice.HOTBAR));
        assertEquals("cursor_pickup_carry", invalidation.diagnostics());
    }

    @Test
    void pickupWithoutOriginFailsClosedToFullProjection() {
        List<WorkspaceInvalidation> invalidations = WorkspaceCursorCommandService.pickupInvalidations(
                ItemIdentity.of("minecraft:stone"),
                null,
                1);

        assertEquals(1, invalidations.size());
        WorkspaceInvalidation invalidation = invalidations.get(0);
        assertTrue(invalidation.requiresFullProjection());
        assertEquals("cursor_pickup_missing_origin", invalidation.diagnostics());
    }

    @Test
    void hotbarQuickMoveUsesResolvedHostStorageInsteadOfRawTerminalSlots() {
        CapturingTerminalProvider provider = new CapturingTerminalProvider(false);
        InventoryIntegrationRegistry.register(provider);
        InventoryIntegrationRegistry.markBootstrapped();
        ServerPlayer player = playerWithTerminalMenu();
        ItemStack source = new ItemStack("minecraft:stone", 12, 64);
        player.getInventory().setItem(0, source);
        Slot craftingGridSlot = player.containerMenu.getSlot(0);

        WorkspaceCommandOutcome outcome = WorkspaceCursorCommandService.crossSurfaceQuickMoveHotbar(player, 0);

        assertTrue(outcome.success(), outcome.diagnostics());
        assertEquals(1, provider.mutations);
        assertEquals(12, provider.insertedCount);
        assertTrue(provider.insertedPlayer == player);
        assertTrue(player.getInventory().getItem(0).isEmpty());
        assertTrue(craftingGridSlot.getItem().isEmpty());
    }

    @Test
    void hotbarQuickMoveFailsClosedWhenResolvedHostStorageRejectsInsert() {
        CapturingTerminalProvider provider = new CapturingTerminalProvider(true);
        InventoryIntegrationRegistry.register(provider);
        InventoryIntegrationRegistry.markBootstrapped();
        ServerPlayer player = playerWithTerminalMenu();
        ItemStack source = new ItemStack("minecraft:stone", 12, 64);
        player.getInventory().setItem(0, source);
        Slot craftingGridSlot = player.containerMenu.getSlot(0);

        WorkspaceCommandOutcome outcome = WorkspaceCursorCommandService.crossSurfaceQuickMoveHotbar(player, 0);

        assertFalse(outcome.success());
        assertEquals("host_rejected_stack", outcome.diagnostics());
        assertEquals(1, provider.mutations);
        assertEquals(12, player.getInventory().getItem(0).getCount());
        assertTrue(craftingGridSlot.getItem().isEmpty());
    }

    private static ServerPlayer playerWithTerminalMenu() {
        ServerPlayer player = new ServerPlayer();
        TerminalMenu menu = new TerminalMenu();
        menu.addTerminalSlot(new Slot(new TestContainer(), 0));
        player.containerMenu = menu;
        return player;
    }

    private static final class CapturingTerminalProvider implements InventoryIntegrationProvider {
        private static final String SOURCE_ID = "ae2:terminal";
        private final boolean rejectInsert;
        private int mutations;
        private int insertedCount;
        private ServerPlayer insertedPlayer;

        private CapturingTerminalProvider(boolean rejectInsert) {
            this.rejectInsert = rejectInsert;
        }

        @Override
        public String providerId() {
            return "test:terminal";
        }

        @Override
        public int priority() {
            return 1000;
        }

        @Override
        public ProviderResult<InventoryHostSession> openHost(
                dev.imagio.slot.inventory.integration.InventoryHostContext context
        ) {
            InventorySourceDescriptor source = InventorySourceDescriptor.builder(SOURCE_ID)
                    .label(Component.literal("Network"))
                    .domain(InventorySourceDomain.HOST_STORAGE)
                    .role(InventorySourceRole.PRIMARY_STORAGE)
                    .logicalSlotCount(1)
                    .bindingRoute(InventoryBindingRoute.PROVIDER)
                    .capabilities(Set.of(InventoryCapability.INSERT))
                    .actionRoute(InventoryActionRoute.PROVIDER_MUTATION)
                    .paneMembership(InventoryPaneMembership.EXTERNAL)
                    .stableOrder(0)
                    .build();
            return ProviderResult.supported(new InventoryHostSession() {
                @Override
                public String providerId() {
                    return "test:terminal";
                }

                @Override
                public List<InventorySourceDescriptor> hostSources() {
                    return List.of(source);
                }

                @Override
                public MutationResult mutate(
                        InventoryHostDescriptor host,
                        InventoryMutationRequest request,
                        InventoryMutationMode mode
                ) {
                    mutations++;
                    insertedPlayer = request.player();
                    insertedCount = request.stack().getCount();
                    return rejectInsert
                            ? MutationResult.blocked("network_rejected", request.stack())
                            : MutationResult.success(ItemStack.EMPTY);
                }
            });
        }
    }

    private static final class TerminalMenu extends AbstractContainerMenu {
        private TerminalMenu() {
            super(null, 0);
        }

        private void addTerminalSlot(Slot slot) {
            addSlot(slot);
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }
    }

    private static final class TestContainer implements Container {
    }
}
