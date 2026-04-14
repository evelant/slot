package dev.imagio.slot.inventory.kernel;

import dev.imagio.slot.client.model.SlotRef;
import dev.imagio.slot.session.ChestLikeMenuLayout;
import dev.imagio.slot.storage.adapter.ExternalStorageStackSnapshot;
import dev.imagio.slot.storage.adapter.StorageTransferMode;
import dev.imagio.slot.storage.provider.StorageViewProviderSession;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MenuSlotResolverTest {
    @Test
    void resolvesLogicalSlotsBeyondPhysicalSlotList() {
        LogicalMenu menu = new LogicalMenu();
        menu.addLogicalSlot(200, stack("minecraft:stone", 8));
        menu.addLogicalSlot(201, stack("minecraft:stone", 2));

        ChestLikeMenuLayout layout = layout(Map.of(
                ChestLikeMenuLayout.SOURCE_PLAYER_MAIN, List.of(200, 201)
        ));
        MenuSlotResolver resolver = new MenuSlotResolver(menu, layout);

        assertTrue(resolver.sourceMenuBacked(ChestLikeMenuLayout.SOURCE_PLAYER_MAIN));
        assertEquals(List.of(200, 201), resolver.menuSlotsForSource(ChestLikeMenuLayout.SOURCE_PLAYER_MAIN));
        assertEquals(201, resolver.resolveMenuSlot(new SlotRef(ChestLikeMenuLayout.SOURCE_PLAYER_MAIN, 1)));
        assertEquals(200, resolver.firstInsertionTarget(List.of(200, 201), stack("minecraft:stone", 4)));
    }

    @Test
    void missingLogicalSlotsFailClosed() {
        LogicalMenu menu = new LogicalMenu();
        ChestLikeMenuLayout layout = layout(Map.of(
                ChestLikeMenuLayout.SOURCE_PLAYER_MAIN, List.of(300)
        ));
        MenuSlotResolver resolver = new MenuSlotResolver(menu, layout);

        assertFalse(resolver.sourceMenuBacked(ChestLikeMenuLayout.SOURCE_PLAYER_MAIN));
        assertTrue(resolver.menuSlotsForSource(ChestLikeMenuLayout.SOURCE_PLAYER_MAIN).isEmpty());
        assertNull(resolver.resolveMenuSlot(ChestLikeMenuLayout.SOURCE_PLAYER_MAIN, 0));
    }

    private static ChestLikeMenuLayout layout(Map<String, List<Integer>> sourceSlots) {
        return new ChestLikeMenuLayout(
                0,
                List.of(),
                "Test",
                sourceSlots,
                sourceIds(sourceSlots),
                true,
                new TestStorageSession()
        );
    }

    private static Map<Integer, String> sourceIds(Map<String, List<Integer>> sourceSlots) {
        LinkedHashMap<Integer, String> resolved = new LinkedHashMap<>();
        sourceSlots.forEach((sourceId, slots) -> {
            for (int slotId : slots) {
                resolved.put(slotId, sourceId);
            }
        });
        return Map.copyOf(resolved);
    }

    private static ItemStack stack(String itemId, int count) {
        return new ItemStack(itemId, count, 64);
    }

    private static final class LogicalMenu extends AbstractContainerMenu {
        private final Map<Integer, Slot> logicalSlots = new LinkedHashMap<>();

        private LogicalMenu() {
            super(null, 0);
        }

        private void addLogicalSlot(int slotId, ItemStack stack) {
            Slot slot = new Slot(new Container() {
            }, slotId, 0, 0);
            slot.set(stack);
            logicalSlots.put(slotId, slot);
        }

        @Override
        public Slot getSlot(int index) {
            Slot logicalSlot = logicalSlots.get(index);
            if (logicalSlot != null) {
                return logicalSlot;
            }
            return super.getSlot(index);
        }

        @Override
        public boolean stillValid(net.minecraft.world.entity.player.Player player) {
            return true;
        }
    }

    private static final class TestStorageSession implements StorageViewProviderSession {
        @Override
        public String providerId() {
            return "test";
        }

        @Override
        public dev.imagio.slot.session.InventorySourceDescriptor primaryStorageSource() {
            return dev.imagio.slot.session.InventorySourceDescriptor.builder(ChestLikeMenuLayout.SOURCE_CARRIED_STORAGE)
                    .label(net.minecraft.network.chat.Component.literal("Test"))
                    .domain(dev.imagio.slot.session.InventorySourceDomain.HOST_STORAGE)
                    .role(dev.imagio.slot.session.InventorySourceRole.PRIMARY_STORAGE)
                    .slotCount(0)
                    .backingKind(dev.imagio.slot.session.InventorySourceBackingKind.MENU_BACKED)
                    .capabilities(java.util.Set.of(
                            dev.imagio.slot.session.InventorySourceCapability.INSERT,
                            dev.imagio.slot.session.InventorySourceCapability.EXTRACT
                    ))
                    .actionable(true)
                    .menuBacked(true)
                    .build();
        }

        @Override
        public boolean primaryStorageIsCarried() {
            return true;
        }

        @Override
        public List<Integer> primaryMenuSlots() {
            return List.of();
        }

        @Override
        public List<ExternalStorageStackSnapshot> readClientPrimarySnapshots(AbstractContainerMenu menu) {
            return List.of();
        }

        @Override
        public ItemStack extractFromPrimary(AbstractContainerMenu menu, ServerPlayer player, dev.imagio.slot.client.model.ItemIdentity identity, StorageTransferMode mode) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertIntoPrimary(AbstractContainerMenu menu, ServerPlayer player, ItemStack stack) {
            return stack;
        }

    }
}
