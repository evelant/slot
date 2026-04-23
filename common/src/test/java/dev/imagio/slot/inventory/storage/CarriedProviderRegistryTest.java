package dev.imagio.slot.inventory.storage;

import dev.imagio.slot.inventory.core.ItemIdentity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CarriedProviderRegistryTest {

    @BeforeEach
    @AfterEach
    void reset() {
        CarriedProviderRegistry.resetForTests();
    }

    @Test
    void registryStartsEmpty() {
        assertTrue(CarriedProviderRegistry.all().isEmpty());
        assertFalse(CarriedProviderRegistry.forSource("anything").isPresent());
    }

    @Test
    void defaultHandlesMatchesOnPrefixSlash() {
        RecordingProvider curios = new RecordingProvider("curios:slot", List.of());
        CarriedProviderRegistry.register(curios);

        assertTrue(curios.handles("curios:slot/ring-0"));
        assertFalse(curios.handles("curios:something-else"));
        assertFalse(curios.handles("other:slot/thing"));
        assertFalse(curios.handles(null));
    }

    @Test
    void forSourceReturnsFirstMatchingProvider() {
        RecordingProvider sb = new RecordingProvider("sophisticatedbackpacks:carried", List.of());
        RecordingProvider curios = new RecordingProvider("curios:slot", List.of());
        CarriedProviderRegistry.register(sb);
        CarriedProviderRegistry.register(curios);

        assertSame(sb, CarriedProviderRegistry.forSource("sophisticatedbackpacks:carried/uuid1").orElseThrow());
        assertSame(curios, CarriedProviderRegistry.forSource("curios:slot/ring-0").orElseThrow());
        assertFalse(CarriedProviderRegistry.forSource("unknown:thing/x").isPresent());
    }

    @Test
    void forSourceReturnsEmptyForBlankOrNull() {
        CarriedProviderRegistry.register(new RecordingProvider("sb:carried", List.of()));
        assertFalse(CarriedProviderRegistry.forSource(null).isPresent());
        assertFalse(CarriedProviderRegistry.forSource("").isPresent());
        assertFalse(CarriedProviderRegistry.forSource("   ").isPresent());
    }

    @Test
    void registrationOrderPreservedInAll() {
        RecordingProvider a = new RecordingProvider("a:x", List.of());
        RecordingProvider b = new RecordingProvider("b:x", List.of());
        RecordingProvider c = new RecordingProvider("c:x", List.of());
        CarriedProviderRegistry.register(a);
        CarriedProviderRegistry.register(b);
        CarriedProviderRegistry.register(c);

        List<CarriedProvider> all = CarriedProviderRegistry.all();
        assertEquals(3, all.size());
        assertSame(a, all.get(0));
        assertSame(b, all.get(1));
        assertSame(c, all.get(2));
    }

    @Test
    void nullProviderRejected() {
        assertThrows(NullPointerException.class, () -> CarriedProviderRegistry.register(null));
    }

    @Test
    void defaultInsertBestFitWalksSourcesInOrder() {
        // Two sources. First fills up completely on insert, second takes the remainder.
        ItemStack stone = new ItemStack("minecraft:stone", 20, 64);
        RecordingProvider provider = new RecordingProvider("test:provider", List.of("test:provider/a", "test:provider/b")) {
            @Override
            public ItemStack insert(ServerPlayer player, String sourceId, ItemStack stack, boolean simulate) {
                insertLog.add(sourceId + ":" + stack.getCount());
                if (sourceId.endsWith("/a")) {
                    // first source accepts 12
                    ItemStack rem = stack.copy();
                    rem.setCount(Math.max(0, stack.getCount() - 12));
                    return rem;
                }
                // second source accepts everything remaining
                ItemStack rem = stack.copy();
                rem.setCount(0);
                return rem;
            }
        };
        CarriedProviderRegistry.register(provider);

        ItemStack remainder = provider.insertBestFit(null, stone, false);
        assertTrue(remainder.isEmpty());
        assertEquals(List.of("test:provider/a:20", "test:provider/b:8"), provider.insertLog);
    }

    @Test
    void defaultFindIdentityStopsAtFirstMatch() {
        ItemStack iron = new ItemStack("minecraft:iron_ingot", 1, 64);
        RecordingProvider provider = new RecordingProvider(
                "test:provider",
                List.of("test:provider/a", "test:provider/b"),
                /*slotCount*/ 2
        ) {
            @Override
            public ItemStack peek(Player player, String sourceId, int slotIndex) {
                if (sourceId.endsWith("/b") && slotIndex == 0) {
                    return iron;
                }
                return ItemStack.EMPTY;
            }
        };
        CarriedProviderRegistry.register(provider);

        Optional<CarriedSourceAccess.CarriedLocation> hit = provider.findIdentity(null, ItemIdentity.of("minecraft:iron_ingot"));
        assertTrue(hit.isPresent());
        assertEquals("test:provider/b", hit.get().sourceId());
        assertEquals(0, hit.get().slotIndex());
    }

    /** Recording test double. Uses an injectable source list; subclasses override hot methods. */
    private static class RecordingProvider implements CarriedProvider {
        private final String prefix;
        private final List<String> sourceIds;
        private final int slotCount;
        final java.util.ArrayList<String> insertLog = new java.util.ArrayList<>();

        RecordingProvider(String prefix, List<String> sourceIds) {
            this(prefix, sourceIds, 0);
        }

        RecordingProvider(String prefix, List<String> sourceIds, int slotCount) {
            this.prefix = prefix;
            this.sourceIds = List.copyOf(sourceIds);
            this.slotCount = slotCount;
        }

        @Override public String prefix() { return prefix; }
        @Override public List<String> sourceIds(Player player) { return sourceIds; }
        @Override public ItemStack peek(Player p, String s, int i) { return ItemStack.EMPTY; }
        @Override public ItemStack extract(ServerPlayer p, String s, int i, int n, boolean sim) { return ItemStack.EMPTY; }
        @Override public ItemStack insert(ServerPlayer p, String s, ItemStack stack, boolean sim) { return stack; }
        @Override public int slotCount(Player p, String s) { return slotCount; }
    }
}
