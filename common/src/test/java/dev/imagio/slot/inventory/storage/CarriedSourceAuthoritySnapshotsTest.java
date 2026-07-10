package dev.imagio.slot.inventory.storage;

import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.InventoryCapability;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CarriedSourceAuthoritySnapshotsTest {
    @BeforeEach
    @AfterEach
    void resetProviders() {
        CarriedProviderRegistry.resetForTests();
    }

    @Test
    void currentAuthorityIncludesBespokeProviderSourcesBeforeBuiltinLanes() {
        ServerPlayer player = new ServerPlayer();
        player.containerMenu = new TestMenu(7);
        player.getInventory().items.set(9, stack("minecraft:cobblestone", 7));

        FakeProvider backpack = new FakeProvider(
                "test:backpack",
                List.of("test:backpack/a"),
                4,
                false);
        backpack.put("test:backpack/a", 2, stack("minecraft:diamond", 3));
        CarriedProviderRegistry.register(backpack);

        InventoryAuthoritySnapshot authority =
                CarriedSourceAuthoritySnapshots.currentAuthority(player, "test");

        assertEquals(
                List.of(
                        "test:backpack/a",
                        BuiltinInventoryIds.PLAYER_MAIN,
                        BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0,
                        BuiltinInventoryIds.PLAYER_ARMOR,
                        BuiltinInventoryIds.PLAYER_OFFHAND),
                authority.carriedSources().stream().map(source -> source.id()).toList());
        assertEquals("minecraft:diamond", authority.slotEntry("test:backpack/a", 2).stack().itemId());
        assertEquals("minecraft:cobblestone",
                authority.slotEntry(BuiltinInventoryIds.PLAYER_MAIN, 0).stack().itemId());
    }

    @Test
    void currentAuthorityUsesProviderMutableSourceIds() {
        ServerPlayer player = new ServerPlayer();
        player.containerMenu = new TestMenu(9);

        FakeProvider backpack = new FakeProvider(
                "sophisticatedbackpacks:carried",
                List.of("sophisticatedbackpacks:carried/test-stable-id"),
                2,
                false);
        backpack.put("sophisticatedbackpacks:carried/test-stable-id", 0, stack("minecraft:redstone", 11));
        CarriedProviderRegistry.register(backpack);

        InventoryAuthoritySnapshot authority =
                CarriedSourceAuthoritySnapshots.currentAuthority(player, "test");

        assertNotNull(authority.source("sophisticatedbackpacks:carried/test-stable-id"));
        assertEquals("minecraft:redstone",
                authority.slotEntry("sophisticatedbackpacks:carried/test-stable-id", 0).stack().itemId());
        assertEquals(11,
                authority.slotEntry("sophisticatedbackpacks:carried/test-stable-id", 0).count());
    }

    @Test
    void currentAuthorityPreservesProviderSourceCapabilities() {
        ServerPlayer player = new ServerPlayer();
        player.containerMenu = new TestMenu(11);

        String sourceId = "sophisticatedbackpacks:carried/test-stable-id#crafting/0";
        FakeProvider backpack = new FakeProvider(
                "sophisticatedbackpacks:carried",
                List.of(sourceId),
                1,
                false) {
            @Override
            public Set<InventoryCapability> capabilities(Player player, String sourceId) {
                return Set.of(InventoryCapability.EXTRACT);
            }
        };
        backpack.put(sourceId, 0, stack("minecraft:stick", 2));
        CarriedProviderRegistry.register(backpack);

        InventoryAuthoritySnapshot authority =
                CarriedSourceAuthoritySnapshots.currentAuthority(player, "test");

        assertNotNull(authority.source(sourceId));
        assertTrue(authority.source(sourceId).supports(InventoryCapability.EXTRACT));
        assertFalse(authority.source(sourceId).supports(InventoryCapability.INSERT));
        assertEquals("minecraft:stick", authority.slotEntry(sourceId, 0).stack().itemId());
    }

    private static ItemStack stack(String itemId, int count) {
        return new ItemStack(itemId, count, 64);
    }

    private static final class TestMenu extends AbstractContainerMenu {
        TestMenu(int containerId) {
            super(null, containerId);
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }
    }

    private static class FakeProvider implements CarriedProvider {
        private final String prefix;
        private final List<String> sourceIds;
        private final int slotCount;
        private final boolean autoSynthesize;
        private final Map<String, Map<Integer, ItemStack>> contents = new LinkedHashMap<>();

        FakeProvider(String prefix, List<String> sourceIds, int slotCount, boolean autoSynthesize) {
            this.prefix = prefix;
            this.sourceIds = List.copyOf(sourceIds);
            this.slotCount = slotCount;
            this.autoSynthesize = autoSynthesize;
        }

        void put(String sourceId, int slotIndex, ItemStack stack) {
            contents.computeIfAbsent(sourceId, ignored -> new LinkedHashMap<>()).put(slotIndex, stack);
        }

        @Override
        public String prefix() {
            return prefix;
        }

        @Override
        public boolean autoSynthesizeExtension() {
            return autoSynthesize;
        }

        @Override
        public List<String> sourceIds(Player player) {
            return sourceIds;
        }

        @Override
        public ItemStack peek(Player player, String sourceId, int slotIndex) {
            return contents.getOrDefault(sourceId, Map.of()).getOrDefault(slotIndex, ItemStack.EMPTY);
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
            return sourceIds.contains(sourceId) ? slotCount : 0;
        }
    }
}
