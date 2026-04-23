package dev.imagio.slot.inventory.storage;

import dev.imagio.slot.inventory.core.InventoryActionRoute;
import dev.imagio.slot.inventory.core.InventoryCapability;
import dev.imagio.slot.inventory.core.InventoryPaneMembership;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.InventorySourceDomain;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.integration.InventoryMutationKind;
import dev.imagio.slot.inventory.integration.InventoryMutationMode;
import dev.imagio.slot.inventory.integration.InventoryMutationRequest;
import dev.imagio.slot.inventory.integration.InventoryTransferMode;
import dev.imagio.slot.inventory.integration.MutationResult;
import dev.imagio.slot.inventory.integration.PlayerInventoryContext;
import dev.imagio.slot.inventory.integration.PlayerInventoryExtension;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultCarriedProviderIntegrationTest {

    private DefaultCarriedProviderIntegration integration;
    private ServerPlayer player;
    private PlayerInventoryContext ctx;

    @BeforeEach
    void setup() {
        CarriedProviderRegistry.resetForTests();
        integration = new DefaultCarriedProviderIntegration();
        player = new ServerPlayer();
        ctx = new PlayerInventoryContext(player.getInventory(), null, "Test", null);
    }

    @AfterEach
    void teardown() {
        CarriedProviderRegistry.resetForTests();
    }

    @Test
    void openHostAlwaysUnsupported() {
        assertTrue(integration.openHost(null).unsupported());
    }

    @Test
    void noProvidersYieldsNoExtensions() {
        assertTrue(integration.playerExtensions(ctx).isEmpty());
    }

    @Test
    void optOutProviderYieldsNoExtension() {
        CarriedProviderRegistry.register(new FakeProvider("custom:bespoke", List.of("custom:bespoke/a"), 4) {
            @Override
            public boolean autoSynthesizeExtension() {
                return false;
            }
        });
        assertTrue(integration.playerExtensions(ctx).isEmpty());
    }

    @Test
    void emittedExtensionExposesProviderSourcesWithSensibleDefaults() {
        FakeProvider curios = new FakeProvider(
                "curios:slot",
                List.of("curios:slot/ring-0", "curios:slot/amulet-0"),
                /*slotCount*/ 1
        );
        CarriedProviderRegistry.register(curios);

        List<PlayerInventoryExtension> extensions = integration.playerExtensions(ctx);
        assertEquals(1, extensions.size());
        PlayerInventoryExtension ext = extensions.get(0);
        assertEquals("slot:auto/curios:slot", ext.providerId());

        List<InventorySourceDescriptor> sources = ext.additionalSources();
        assertEquals(2, sources.size());

        InventorySourceDescriptor first = sources.get(0);
        assertEquals("curios:slot/ring-0", first.id());
        assertEquals(InventorySourceDomain.PLAYER_EXTENSION, first.domain());
        assertEquals(InventoryPaneMembership.CARRIED, first.paneMembership());
        assertEquals(InventoryActionRoute.PROVIDER_MUTATION, first.actionRoute());
        assertTrue(first.capabilities().contains(InventoryCapability.INSERT));
        assertTrue(first.capabilities().contains(InventoryCapability.EXTRACT));
        assertEquals(1, first.logicalSlotCount());
        // stableOrder must be distinct + stable between the two sources
        assertFalse(first.stableOrder() == sources.get(1).stableOrder());
    }

    @Test
    void mutateInsertDelegatesToProvider() {
        FakeProvider fake = new FakeProvider("custom:store", List.of("custom:store/a"), 4);
        CarriedProviderRegistry.register(fake);
        PlayerInventoryExtension ext = integration.playerExtensions(ctx).get(0);

        ItemStack stack = new ItemStack("minecraft:stone", 10, 64);
        InventoryMutationRequest request = InventoryMutationRequest.insert(null, player, "custom:store/a", stack);

        MutationResult result = ext.mutate(null, request, InventoryMutationMode.EXECUTE);
        assertTrue(result.successful());
        assertEquals(1, fake.insertCalls.size());
        assertEquals("custom:store/a:stone:10", fake.insertCalls.get(0));
    }

    @Test
    void mutateInsertSimulateFailsWhenRemainderNonEmpty() {
        FakeProvider fake = new FakeProvider("custom:store", List.of("custom:store/a"), 4) {
            @Override
            public ItemStack insert(ServerPlayer player, String sourceId, ItemStack stack, boolean simulate) {
                // Pretend only half fits
                ItemStack remainder = stack.copy();
                remainder.setCount(stack.getCount() / 2);
                return remainder;
            }
        };
        CarriedProviderRegistry.register(fake);
        PlayerInventoryExtension ext = integration.playerExtensions(ctx).get(0);

        ItemStack stack = new ItemStack("minecraft:stone", 10, 64);
        InventoryMutationRequest request = InventoryMutationRequest.insert(null, player, "custom:store/a", stack);

        MutationResult simulate = ext.mutate(null, request, InventoryMutationMode.SIMULATE);
        assertFalse(simulate.successful());
        assertEquals("simulation_incomplete", simulate.diagnostics());
    }

    @Test
    void mutateExtractBySlotCallsProviderExtract() {
        FakeProvider fake = new FakeProvider("custom:store", List.of("custom:store/a"), 4);
        fake.contents.get("custom:store/a").put(2, new ItemStack("minecraft:diamond", 3, 64));
        CarriedProviderRegistry.register(fake);
        PlayerInventoryExtension ext = integration.playerExtensions(ctx).get(0);

        InventoryMutationRequest request = new InventoryMutationRequest(
                InventoryMutationKind.EXTRACT,
                "custom:store/a",
                /*slotIndex*/ 2,
                "",
                /*requestedCount*/ 2,
                null,
                ItemStack.EMPTY,
                InventoryTransferMode.ONE,
                "",
                null,
                player
        );

        MutationResult result = ext.mutate(null, request, InventoryMutationMode.EXECUTE);
        assertTrue(result.successful());
        assertEquals(2, result.stackRemainder().getCount());
        assertEquals("minecraft:diamond", result.stackRemainder().itemId());
    }

    @Test
    void mutateExtractByIdentityFindsFirstMatchAcrossSources() {
        FakeProvider fake = new FakeProvider("custom:store",
                List.of("custom:store/a", "custom:store/b"), 4);
        fake.contents.get("custom:store/b").put(1, new ItemStack("minecraft:gold_ingot", 5, 64));
        CarriedProviderRegistry.register(fake);
        PlayerInventoryExtension ext = integration.playerExtensions(ctx).get(0);

        InventoryMutationRequest request = new InventoryMutationRequest(
                InventoryMutationKind.EXTRACT,
                "custom:store/a",
                /*slotIndex*/ -1,
                "",
                /*requestedCount*/ 0,
                ItemIdentity.of("minecraft:gold_ingot"),
                ItemStack.EMPTY,
                InventoryTransferMode.STACK,
                "",
                null,
                player
        );

        MutationResult result = ext.mutate(null, request, InventoryMutationMode.EXECUTE);
        assertTrue(result.successful());
        assertEquals("minecraft:gold_ingot", result.stackRemainder().itemId());
        assertEquals(5, result.stackRemainder().getCount());
    }

    @Test
    void mutateRejectsUnknownSourceId() {
        FakeProvider fake = new FakeProvider("custom:store", List.of("custom:store/a"), 4);
        CarriedProviderRegistry.register(fake);
        PlayerInventoryExtension ext = integration.playerExtensions(ctx).get(0);

        ItemStack stack = new ItemStack("minecraft:stone", 1, 64);
        InventoryMutationRequest request = InventoryMutationRequest.insert(null, player, "sophisticatedbackpacks:carried/x", stack);

        MutationResult result = ext.mutate(null, request, InventoryMutationMode.EXECUTE);
        assertFalse(result.successful());
        assertEquals("unsupported_source", result.diagnostics());
    }

    @Test
    void providerSourceIdsReadOnceAtExtensionConstruction() {
        // Confirms extensions are per-call, so re-invoking playerExtensions
        // picks up a provider's updated source set.
        MutableSourceProvider mut = new MutableSourceProvider();
        mut.sources = List.of("mut:x/a");
        CarriedProviderRegistry.register(mut);

        PlayerInventoryExtension first = integration.playerExtensions(ctx).get(0);
        assertEquals(1, first.additionalSources().size());

        mut.sources = List.of("mut:x/a", "mut:x/b");
        PlayerInventoryExtension second = integration.playerExtensions(ctx).get(0);
        assertEquals(2, second.additionalSources().size());
    }

    /**
     * Minimal test double. Keeps a map of sourceId → (slotIndex → stack)
     * for deterministic extract/insert behaviour.
     */
    private static class FakeProvider implements CarriedProvider {
        private final String prefix;
        private final List<String> sourceIds;
        private final int slotCount;
        final Map<String, Map<Integer, ItemStack>> contents = new LinkedHashMap<>();
        final List<String> insertCalls = new ArrayList<>();

        FakeProvider(String prefix, List<String> sourceIds, int slotCount) {
            this.prefix = prefix;
            this.sourceIds = List.copyOf(sourceIds);
            this.slotCount = slotCount;
            for (String id : sourceIds) {
                contents.put(id, new LinkedHashMap<>());
            }
        }

        @Override public String prefix() { return prefix; }
        @Override public List<String> sourceIds(Player player) { return sourceIds; }
        @Override public int slotCount(Player player, String sourceId) { return sourceIds.contains(sourceId) ? slotCount : 0; }

        @Override
        public ItemStack peek(Player player, String sourceId, int slotIndex) {
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
        public ItemStack insert(ServerPlayer player, String sourceId, ItemStack stack, boolean simulate) {
            insertCalls.add(sourceId + ":" + stack.itemId().replace("minecraft:", "") + ":" + stack.getCount());
            // Default fake: accept everything
            ItemStack rem = stack.copy();
            rem.setCount(0);
            return rem;
        }
    }

    private static class MutableSourceProvider implements CarriedProvider {
        volatile List<String> sources = List.of();
        @Override public String prefix() { return "mut:x"; }
        @Override public List<String> sourceIds(Player player) { return sources; }
        @Override public int slotCount(Player player, String sourceId) { return 1; }
        @Override public ItemStack peek(Player p, String s, int i) { return ItemStack.EMPTY; }
        @Override public ItemStack extract(ServerPlayer p, String s, int i, int n, boolean sim) { return ItemStack.EMPTY; }
        @Override public ItemStack insert(ServerPlayer p, String s, ItemStack stack, boolean sim) { return stack; }
    }
}
