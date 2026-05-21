package dev.imagio.slot.inventory.storage;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CarriedInventoryRevisionsTest {
    @BeforeEach
    void resetRevisions() {
        CarriedInventoryRevisions.clear();
    }

    @AfterEach
    void cleanupRevisions() {
        CarriedInventoryRevisions.clear();
    }

    @Test
    void cachedPressureIsReusedUntilPlayerRevisionChanges() {
        ServerPlayer player = new ServerPlayer();
        CountingCarriedSourceAccess carried = new CountingCarriedSourceAccess();
        carried.pressure = new CarriedSourceAccess.CarriedStoragePressure(100, 80);

        CarriedSourceAccess.CarriedStoragePressure first =
                CarriedInventoryRevisions.cachedPressure(player, carried);
        carried.pressure = new CarriedSourceAccess.CarriedStoragePressure(100, 20);
        CarriedSourceAccess.CarriedStoragePressure second =
                CarriedInventoryRevisions.cachedPressure(player, carried);

        assertEquals(80, first.occupiedSlots());
        assertEquals(80, second.occupiedSlots());
        assertEquals(1, carried.pressureCalls);

        CarriedInventoryRevisions.markChanged(player, "test_mutation");
        CarriedSourceAccess.CarriedStoragePressure afterMutation =
                CarriedInventoryRevisions.cachedPressure(player, carried);

        assertEquals(20, afterMutation.occupiedSlots());
        assertEquals(2, carried.pressureCalls);
    }

    @Test
    void uuidSignalInvalidatesTheSamePlayerCache() {
        ServerPlayer player = new ServerPlayer();
        CountingCarriedSourceAccess carried = new CountingCarriedSourceAccess();
        carried.pressure = new CarriedSourceAccess.CarriedStoragePressure(10, 8);

        CarriedInventoryRevisions.cachedPressure(player, carried);
        carried.pressure = new CarriedSourceAccess.CarriedStoragePressure(10, 3);
        CarriedInventoryRevisions.markChanged(player.getUUID(), "menu_slot_changed");
        CarriedSourceAccess.CarriedStoragePressure refreshed =
                CarriedInventoryRevisions.cachedPressure(player, carried);

        assertEquals(3, refreshed.occupiedSlots());
        assertEquals(2, carried.pressureCalls);
    }

    @Test
    void forgetClearsCachedPressureAndRevision() {
        ServerPlayer player = new ServerPlayer();
        CountingCarriedSourceAccess carried = new CountingCarriedSourceAccess();
        carried.pressure = new CarriedSourceAccess.CarriedStoragePressure(10, 8);

        CarriedInventoryRevisions.cachedPressure(player, carried);
        CarriedInventoryRevisions.markChanged(player, "test_mutation");
        CarriedInventoryRevisions.forget(player);
        carried.pressure = new CarriedSourceAccess.CarriedStoragePressure(10, 4);
        CarriedSourceAccess.CarriedStoragePressure refreshed =
                CarriedInventoryRevisions.cachedPressure(player, carried);

        assertEquals(0, CarriedInventoryRevisions.revision(player));
        assertEquals(4, refreshed.occupiedSlots());
        assertEquals(2, carried.pressureCalls);
    }

    private static final class CountingCarriedSourceAccess implements CarriedSourceAccess {
        CarriedSourceAccess.CarriedStoragePressure pressure = CarriedSourceAccess.CarriedStoragePressure.empty();
        int pressureCalls;

        @Override
        public ItemStack peek(ServerPlayer player, String sourceId, int slotIndex) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack extract(ServerPlayer player, String sourceId, int slotIndex, int amount, boolean simulate) {
            return ItemStack.EMPTY;
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
            return Optional.empty();
        }

        @Override
        public List<CarriedLocation> findAllMatching(ServerPlayer player, ItemIdentity identity) {
            return List.of();
        }

        @Override
        public InventoryAuthoritySnapshot currentAuthority(ServerPlayer player) {
            return InventoryAuthoritySnapshot.empty();
        }

        @Override
        public CarriedSourceAccess.CarriedStoragePressure carriedStoragePressure(ServerPlayer player) {
            pressureCalls++;
            return pressure;
        }
    }
}
