package dev.imagio.slot.inventory.storage;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StorageAccessRegistryTest {

    @BeforeEach
    void reset() {
        StorageAccessRegistry.resetForTests();
    }

    @AfterEach
    void cleanup() {
        StorageAccessRegistry.resetForTests();
    }

    @Test
    void gettersThrowBeforeInstall() {
        assertFalse(StorageAccessRegistry.isInstalled());
        assertThrows(IllegalStateException.class, StorageAccessRegistry::carriedSourceAccess);
        assertThrows(IllegalStateException.class, StorageAccessRegistry::worldStorageAccess);
    }

    @Test
    void installedAccessorsRoundTripThroughGetters() {
        CarriedSourceAccess carried = new NoOpCarriedSourceAccess();
        WorldStorageAccess world = new NoOpWorldStorageAccess();
        StorageAccessRegistry.installCarriedSourceAccess(carried);
        StorageAccessRegistry.installWorldStorageAccess(world);

        assertTrue(StorageAccessRegistry.isInstalled());
        assertSame(carried, StorageAccessRegistry.carriedSourceAccess());
        assertSame(world, StorageAccessRegistry.worldStorageAccess());
    }

    @Test
    void reinstallReplacesPriorImplementation() {
        CarriedSourceAccess first = new NoOpCarriedSourceAccess();
        CarriedSourceAccess second = new NoOpCarriedSourceAccess();
        StorageAccessRegistry.installCarriedSourceAccess(first);
        StorageAccessRegistry.installCarriedSourceAccess(second);
        assertSame(second, StorageAccessRegistry.carriedSourceAccess());
    }

    @Test
    void nullInstallRejectsWithNPE() {
        assertThrows(NullPointerException.class,
                () -> StorageAccessRegistry.installCarriedSourceAccess(null));
        assertThrows(NullPointerException.class,
                () -> StorageAccessRegistry.installWorldStorageAccess(null));
    }

    private static final class NoOpCarriedSourceAccess implements CarriedSourceAccess {
        @Override public ItemStack peek(ServerPlayer p, String id, int i) { return ItemStack.EMPTY; }
        @Override public ItemStack extract(ServerPlayer p, String id, int i, int n, boolean s) { return ItemStack.EMPTY; }
        @Override public ItemStack insertBestFit(ServerPlayer p, ItemStack s, boolean sim) { return s; }
        @Override public ItemStack insertIntoProviders(ServerPlayer p, ItemStack s, boolean sim) { return s; }
        @Override public Optional<CarriedLocation> findIdentity(ServerPlayer p, ItemIdentity id) { return Optional.empty(); }
        @Override public List<CarriedLocation> findAllMatching(ServerPlayer p, ItemIdentity id) { return List.of(); }
        @Override public InventoryAuthoritySnapshot currentAuthority(ServerPlayer p) { return InventoryAuthoritySnapshot.empty(); }
    }

    private static final class NoOpWorldStorageAccess implements WorldStorageAccess {
        @Override public ItemStack insert(MinecraftServer s, Target t, ItemStack stk, boolean sim) { return stk; }
        @Override public ItemStack extract(MinecraftServer s, Target t, int i, int n, boolean sim) { return ItemStack.EMPTY; }
        @Override public List<SlotContent> enumerate(MinecraftServer s, Target t) { return List.of(); }
        @Override public int slotCount(MinecraftServer s, Target t) { return 0; }
        @Override public boolean isAccessible(MinecraftServer s, Target t) { return false; }
        @Override public void registerDelegate(Delegate delegate) {}
    }
}
