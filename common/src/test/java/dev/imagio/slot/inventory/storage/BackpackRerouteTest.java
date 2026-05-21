package dev.imagio.slot.inventory.storage;

import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BackpackRerouteTest {
    @BeforeEach
    void resetRegistries() {
        StorageAccessRegistry.resetForTests();
        CarriedProviderRegistry.resetForTests();
    }

    @AfterEach
    void cleanupRegistries() {
        StorageAccessRegistry.resetForTests();
        CarriedProviderRegistry.resetForTests();
    }

    @Test
    void successfulPickupRerouteDoesNotEnumerateProvidersForDiagnosticsOrShrink() {
        ServerPlayer player = new ServerPlayer();
        player.getInventory().items.set(9, stack("minecraft:oak_log", 32));
        CountingProvider provider = new CountingProvider();
        FakeCarriedSourceAccess carried = new FakeCarriedSourceAccess(ItemStack.EMPTY);
        CarriedProviderRegistry.register(provider);
        StorageAccessRegistry.installCarriedSourceAccess(carried);
        StorageAccessRegistry.installWorldStorageAccess(new NoOpWorldStorageAccess());

        int routed = BackpackReroute.routeToBackpack(player, stack("minecraft:oak_log", 32), 32);

        assertEquals(32, routed);
        assertEquals(1, carried.insertIntoProvidersCalls);
        assertEquals(0, carried.findAllMatchingCalls);
        assertEquals(0, provider.sourceIdsCalls);
        assertEquals(0, provider.slotCountCalls);
        assertEquals(0, provider.peekCalls);
        assertEquals(0, player.getInventory().items.get(9).getCount());
    }

    @Test
    void failedPickupRerouteStillBuildsProviderDiagnostics() {
        ServerPlayer player = new ServerPlayer();
        player.getInventory().items.set(9, stack("minecraft:oak_log", 32));
        CountingProvider provider = new CountingProvider();
        provider.visibleStack = stack("minecraft:stone", 64);
        FakeCarriedSourceAccess carried = new FakeCarriedSourceAccess(stack("minecraft:oak_log", 32));
        CarriedProviderRegistry.register(provider);
        StorageAccessRegistry.installCarriedSourceAccess(carried);
        StorageAccessRegistry.installWorldStorageAccess(new NoOpWorldStorageAccess());

        int routed = BackpackReroute.routeToBackpack(player, stack("minecraft:oak_log", 32), 32);

        assertEquals(0, routed);
        assertEquals(2, carried.insertIntoProvidersCalls);
        assertEquals(1, provider.sourceIdsCalls);
        assertEquals(1, provider.slotCountCalls);
        assertEquals(1, provider.peekCalls);
        assertEquals(32, player.getInventory().items.get(9).getCount());
    }

    private static ItemStack stack(String itemId, int count) {
        return new ItemStack(itemId, count, 64);
    }

    private static final class FakeCarriedSourceAccess implements CarriedSourceAccess {
        private final ItemStack providerRemainder;
        int insertIntoProvidersCalls;
        int findAllMatchingCalls;

        FakeCarriedSourceAccess(ItemStack providerRemainder) {
            this.providerRemainder = providerRemainder == null ? ItemStack.EMPTY : providerRemainder;
        }

        @Override
        public ItemStack peek(ServerPlayer player, String sourceId, int slotIndex) {
            if (player == null || sourceId == null) {
                return ItemStack.EMPTY;
            }
            Inventory inventory = player.getInventory();
            if (BuiltinInventoryIds.PLAYER_MAIN.equals(sourceId)) {
                int raw = slotIndex + 9;
                return raw >= 0 && raw < inventory.items.size() ? inventory.items.get(raw) : ItemStack.EMPTY;
            }
            if (BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0.equals(sourceId)) {
                return slotIndex >= 0 && slotIndex < 9 ? inventory.items.get(slotIndex) : ItemStack.EMPTY;
            }
            if (BuiltinInventoryIds.PLAYER_OFFHAND.equals(sourceId)) {
                return slotIndex == 0 ? inventory.offhand.get(0) : ItemStack.EMPTY;
            }
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack extract(ServerPlayer player, String sourceId, int slotIndex, int amount, boolean simulate) {
            ItemStack current = peek(player, sourceId, slotIndex);
            if (current.isEmpty() || amount <= 0) {
                return ItemStack.EMPTY;
            }
            int take = Math.min(amount, current.getCount());
            ItemStack extracted = new ItemStack(current.itemId(), take, current.getMaxStackSize());
            if (!simulate) {
                current.shrink(take);
            }
            return extracted;
        }

        @Override
        public ItemStack insertBestFit(ServerPlayer player, ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public ItemStack insertIntoProviders(ServerPlayer player, ItemStack stack, boolean simulate) {
            insertIntoProvidersCalls++;
            return providerRemainder.copy();
        }

        @Override
        public Optional<CarriedLocation> findIdentity(ServerPlayer player, ItemIdentity identity) {
            return Optional.empty();
        }

        @Override
        public List<CarriedLocation> findAllMatching(ServerPlayer player, ItemIdentity identity) {
            findAllMatchingCalls++;
            return List.of();
        }

        @Override
        public InventoryAuthoritySnapshot currentAuthority(ServerPlayer player) {
            return InventoryAuthoritySnapshot.empty();
        }
    }

    private static final class CountingProvider implements CarriedProvider {
        int sourceIdsCalls;
        int slotCountCalls;
        int peekCalls;
        ItemStack visibleStack = ItemStack.EMPTY;

        @Override
        public String prefix() {
            return "test:provider";
        }

        @Override
        public List<String> sourceIds(Player player) {
            sourceIdsCalls++;
            return List.of("test:provider/main");
        }

        @Override
        public ItemStack peek(Player player, String sourceId, int slotIndex) {
            peekCalls++;
            return visibleStack;
        }

        @Override
        public ItemStack extract(ServerPlayer player, String sourceId, int slotIndex, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack insert(ServerPlayer player, String sourceId, ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public int slotCount(Player player, String sourceId) {
            slotCountCalls++;
            return 1;
        }
    }

    private static final class NoOpWorldStorageAccess implements WorldStorageAccess {
        @Override
        public ItemStack insert(MinecraftServer server, Target target, ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public ItemStack extract(MinecraftServer server, Target target, int slotIndex, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public List<SlotContent> enumerate(MinecraftServer server, Target target) {
            return List.of();
        }

        @Override
        public int slotCount(MinecraftServer server, Target target) {
            return 0;
        }

        @Override
        public boolean isAccessible(MinecraftServer server, Target target) {
            return false;
        }

        @Override
        public void registerDelegate(Delegate delegate) {
        }
    }
}
