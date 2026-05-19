package dev.imagio.slot.inventory.storage;

import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Contract test for the {@link CarriedSourceAccess} interface semantics.
 * Exercises the behaviours every platform implementation must preserve —
 * stableOrder preference, finding the first match, enumerating all matches,
 * mixing builtin + provider sources without special-casing.
 *
 * <p>Uses a deterministic in-memory fake backed by a map of
 * {@code sourceId → List<ItemStack>}. Real platform implementations add
 * dispatch-by-source-type on top, but the contract below must hold.
 */
class CarriedSourceAccessContractTest {
    private static final String BACKPACK_SOURCE = "sophisticatedbackpacks:carried/test";
    private static final String CURIOS_SOURCE = "curios:slot/ring-0";

    @Test
    void findIdentityReturnsFirstMatchInStableOrder() {
        // stableOrder preference: provider-backed overflow storage comes
        // before main/hotbar/offhand so workspace lanes stay intact.
        FakeCarriedSourceAccess carried = new FakeCarriedSourceAccess(java.util.List.of(
                BACKPACK_SOURCE,
                BuiltinInventoryIds.PLAYER_MAIN,
                BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0,
                BuiltinInventoryIds.PLAYER_OFFHAND
        ));
        carried.put(BuiltinInventoryIds.PLAYER_MAIN, 5, stack("minecraft:redstone", 16));
        carried.put(BACKPACK_SOURCE, 3, stack("minecraft:redstone", 8));

        Optional<CarriedSourceAccess.CarriedLocation> found = carried.findIdentity(null, identity("minecraft:redstone"));
        assertTrue(found.isPresent());
        assertEquals(BACKPACK_SOURCE, found.get().sourceId());
        assertEquals(3, found.get().slotIndex());
    }

    @Test
    void findIdentityFallsThroughToBackpackWhenAbsentFromVanilla() {
        // No match in main/hotbar/offhand — must walk into backpacks.
        FakeCarriedSourceAccess carried = new FakeCarriedSourceAccess(java.util.List.of(
                BuiltinInventoryIds.PLAYER_MAIN,
                BACKPACK_SOURCE
        ));
        carried.put(BACKPACK_SOURCE, 12, stack("minecraft:diamond", 3));

        Optional<CarriedSourceAccess.CarriedLocation> found = carried.findIdentity(null, identity("minecraft:diamond"));
        assertTrue(found.isPresent());
        assertEquals(BACKPACK_SOURCE, found.get().sourceId());
        assertEquals(12, found.get().slotIndex());
    }

    @Test
    void findAllMatchingReturnsEveryOccurrenceInStableOrder() {
        FakeCarriedSourceAccess carried = new FakeCarriedSourceAccess(java.util.List.of(
                BACKPACK_SOURCE,
                BuiltinInventoryIds.PLAYER_MAIN,
                CURIOS_SOURCE
        ));
        carried.put(BuiltinInventoryIds.PLAYER_MAIN, 0, stack("minecraft:coal", 64));
        carried.put(BACKPACK_SOURCE, 2, stack("minecraft:coal", 32));
        carried.put(CURIOS_SOURCE, 0, stack("minecraft:coal", 1));

        List<CarriedSourceAccess.CarriedLocation> hits = carried.findAllMatching(null, identity("minecraft:coal"));
        assertEquals(3, hits.size());
        assertEquals(BACKPACK_SOURCE, hits.get(0).sourceId());
        assertEquals(BuiltinInventoryIds.PLAYER_MAIN, hits.get(1).sourceId());
        assertEquals(CURIOS_SOURCE, hits.get(2).sourceId());
    }

    @Test
    void movableIdentityMatchesComponentBearingStacks() {
        FakeCarriedSourceAccess carried = new FakeCarriedSourceAccess(java.util.List.of(BACKPACK_SOURCE));
        carried.put(BACKPACK_SOURCE, 0, new ItemStack(
                "sns:straw_basket",
                "{Inventory:[{Slot:0b,id:\"minecraft:torch\",Count:8b}]}",
                1,
                1));

        Optional<CarriedSourceAccess.CarriedLocation> found =
                carried.findIdentity(null, ItemIdentity.of("sns:straw_basket"));

        assertTrue(found.isPresent());
        assertEquals(BACKPACK_SOURCE, found.get().sourceId());
        assertEquals(0, found.get().slotIndex());
    }

    @Test
    void findIdentityWithNoMatchReturnsEmpty() {
        FakeCarriedSourceAccess carried = new FakeCarriedSourceAccess(java.util.List.of(
                BuiltinInventoryIds.PLAYER_MAIN));
        carried.put(BuiltinInventoryIds.PLAYER_MAIN, 0, stack("minecraft:stone", 1));
        assertFalse(carried.findIdentity(null, identity("minecraft:diamond")).isPresent());
        assertTrue(carried.findAllMatching(null, identity("minecraft:diamond")).isEmpty());
    }

    @Test
    void newProviderSourceTypesWorkWithoutChangingContract() {
        // Simulated "future storage mod" — a new source id like "curios:slot/ring".
        // findIdentity / findAllMatching must walk it like any other carried
        // source; we do NOT special-case it or require code changes.
        FakeCarriedSourceAccess carried = new FakeCarriedSourceAccess(java.util.List.of(
                BuiltinInventoryIds.PLAYER_MAIN,
                CURIOS_SOURCE,
                "my-future-mod:pocket-dim/slot-0"
        ));
        carried.put("my-future-mod:pocket-dim/slot-0", 0, stack("minecraft:netherite_ingot", 2));

        Optional<CarriedSourceAccess.CarriedLocation> found = carried.findIdentity(null, identity("minecraft:netherite_ingot"));
        assertTrue(found.isPresent());
        assertEquals("my-future-mod:pocket-dim/slot-0", found.get().sourceId());
    }

    private static ItemIdentity identity(String itemId) {
        return ItemIdentity.of(itemId);
    }

    private static ItemStack stack(String itemId, int count) {
        return new ItemStack(itemId, count, 64);
    }

    /**
     * Deterministic in-memory fake. Sources are declared in
     * stableOrder at construction time. {@code put(sourceId, slotIndex, stack)}
     * populates a slot; ops walk sources in declared order.
     */
    private static final class FakeCarriedSourceAccess implements CarriedSourceAccess {
        private final List<String> sourceOrder;
        private final Map<String, Map<Integer, ItemStack>> contents = new LinkedHashMap<>();

        FakeCarriedSourceAccess(List<String> sourceOrder) {
            this.sourceOrder = List.copyOf(sourceOrder);
            for (String id : sourceOrder) {
                contents.put(id, new LinkedHashMap<>());
            }
        }

        void put(String sourceId, int slotIndex, ItemStack stack) {
            contents.computeIfAbsent(sourceId, k -> new LinkedHashMap<>()).put(slotIndex, stack);
        }

        @Override
        public ItemStack peek(ServerPlayer player, String sourceId, int slotIndex) {
            Map<Integer, ItemStack> src = contents.get(sourceId);
            if (src == null) return ItemStack.EMPTY;
            ItemStack s = src.get(slotIndex);
            return s == null ? ItemStack.EMPTY : s;
        }

        @Override
        public ItemStack extract(ServerPlayer player, String sourceId, int slotIndex, int amount, boolean simulate) {
            ItemStack current = peek(player, sourceId, slotIndex);
            if (current.isEmpty()) return ItemStack.EMPTY;
            int take = Math.min(amount, current.getCount());
            ItemStack taken = new ItemStack(current.itemId(), take, current.getMaxStackSize());
            if (!simulate) {
                int remaining = current.getCount() - take;
                if (remaining <= 0) {
                    contents.get(sourceId).remove(slotIndex);
                } else {
                    contents.get(sourceId).put(slotIndex,
                            new ItemStack(current.itemId(), remaining, current.getMaxStackSize()));
                }
            }
            return taken;
        }

        @Override
        public ItemStack insertBestFit(ServerPlayer player, ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public ItemStack insertIntoProviders(ServerPlayer player, ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public Optional<CarriedLocation> findIdentity(ServerPlayer player, ItemIdentity identity) {
            for (String sourceId : sourceOrder) {
                Map<Integer, ItemStack> src = contents.get(sourceId);
                if (src == null) continue;
                for (Map.Entry<Integer, ItemStack> e : src.entrySet()) {
                    if (e.getValue() == null || e.getValue().isEmpty()) continue;
                    if (ItemIdentityMatcher.matchesMovable(e.getValue(), identity)) {
                        return Optional.of(new CarriedLocation(sourceId, e.getKey()));
                    }
                }
            }
            return Optional.empty();
        }

        @Override
        public List<CarriedLocation> findAllMatching(ServerPlayer player, ItemIdentity identity) {
            List<CarriedLocation> out = new ArrayList<>();
            for (String sourceId : sourceOrder) {
                Map<Integer, ItemStack> src = contents.get(sourceId);
                if (src == null) continue;
                for (Map.Entry<Integer, ItemStack> e : src.entrySet()) {
                    if (e.getValue() == null || e.getValue().isEmpty()) continue;
                    if (ItemIdentityMatcher.matchesMovable(e.getValue(), identity)) {
                        out.add(new CarriedLocation(sourceId, e.getKey()));
                    }
                }
            }
            return List.copyOf(out);
        }

        @Override
        public InventoryAuthoritySnapshot currentAuthority(ServerPlayer player) {
            return InventoryAuthoritySnapshot.empty();
        }
    }
}
