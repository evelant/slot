package dev.imagio.slot.inventory.storage;

import dev.imagio.slot.inventory.core.ItemIdentity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

/**
 * SPI for a single family of carried storage (Sophisticated Backpacks,
 * Curios, a future travelers-backpack mod, …). Providers are registered with
 * {@link CarriedProviderRegistry} at mod init. {@link CarriedSourceAccess}
 * dispatches any non-builtin source id through the registered providers, so
 * adding a new carried storage is additive: implement this interface,
 * register, done — no edits to executors, UI sessions, or the platform
 * implementation.
 *
 * <p>Each provider owns a stable {@link #prefix()} that its source ids start
 * with. The default {@link #handles(String)} matches on {@code prefix() + "/"},
 * which fits the project convention (e.g.
 * {@code sophisticatedbackpacks:carried/<uuid>},
 * {@code curios:slot/ring-0}).
 *
 * <h2>Authority-snapshot integration</h2>
 * Read-side methods ({@link #sourceIds}, {@link #peek}, {@link #slotCount},
 * {@link #findIdentity}, {@link #findAllMatching}) accept the parent
 * {@link Player} type so the same provider can serve both client-side
 * {@code LocalPlayer} (for UI host-descriptor construction) and server-side
 * {@code ServerPlayer} (for authority reads). Mutation methods
 * ({@link #extract}, {@link #insert}, {@link #insertBestFit}) require
 * {@link ServerPlayer} since they may only run on the authoritative side.
 *
 * <h2>Auto-synthesised {@code PlayerInventoryExtension}</h2>
 * For minimal carried mods the framework can auto-generate a
 * {@code PlayerInventoryExtension} that exposes this provider's sources in
 * {@link dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot#carriedSources()}
 * and routes {@code mutate()} back to {@link #extract} / {@link #insert}.
 * Providers that ship their own bespoke {@code InventoryIntegrationProvider}
 * (typically ones with an openable host menu, tool upgrades, or custom
 * labels — Sophisticated Backpacks is the canonical example) should override
 * {@link #autoSynthesizeExtension()} to return {@code false} to avoid
 * double-registering sources.
 */
public interface CarriedProvider {

    /** Stable, lowercase prefix uniquely identifying this provider. */
    String prefix();

    /**
     * Does this provider own the given source id? Default implementation
     * returns true iff {@code sourceId} starts with {@code prefix() + "/"}.
     * Project convention: source ids look like {@code <prefix>/<stableId>}
     * (e.g. {@code sophisticatedbackpacks:carried/<uuid>},
     * {@code curios:slot/ring-0}). Override only for non-standard formats.
     */
    default boolean handles(String sourceId) {
        return sourceId != null && sourceId.startsWith(prefix() + "/");
    }

    /**
     * Enumerate the carried source ids this provider exposes for the given
     * player, in stable order. Accepts the parent {@link Player} type so the
     * same logic serves both client (for host descriptor construction) and
     * server (for authority reads).
     */
    List<String> sourceIds(Player player);

    ItemStack peek(Player player, String sourceId, int slotIndex);

    ItemStack extract(ServerPlayer player, String sourceId, int slotIndex, int amount, boolean simulate);

    /**
     * Insert {@code stack} into {@code sourceId} using best-fit semantics
     * (fill matching stacks first, then empty slots). Returns the remainder
     * that did not fit. Callers that want to insert into any of this
     * provider's sources should use {@link #insertBestFit}.
     */
    ItemStack insert(ServerPlayer player, String sourceId, ItemStack stack, boolean simulate);

    /**
     * Insert {@code stack} into any of this provider's sources for the given
     * player, walking {@link #sourceIds(Player)} in order. Returns the
     * remainder that did not fit. Default implementation iterates each source
     * calling {@link #insert}; override if a provider can do better.
     */
    default ItemStack insertBestFit(ServerPlayer player, ItemStack stack, boolean simulate) {
        if (stack == null || stack.isEmpty()) {
            return stack == null ? ItemStack.EMPTY : stack;
        }
        ItemStack remaining = stack.copy();
        for (String sourceId : sourceIds(player)) {
            if (remaining.isEmpty()) {
                break;
            }
            remaining = insert(player, sourceId, remaining, simulate);
            if (remaining == null) {
                remaining = ItemStack.EMPTY;
            }
        }
        return remaining;
    }

    /**
     * Find the first slot within this provider's sources holding a stack
     * matching {@code identity}, walking {@link #sourceIds(Player)}
     * in order. Default implementation calls {@link #peek} per slot; override
     * if the provider can short-circuit.
     */
    default Optional<CarriedSourceAccess.CarriedLocation> findIdentity(Player player, ItemIdentity identity) {
        if (identity == null) {
            return Optional.empty();
        }
        for (String sourceId : sourceIds(player)) {
            int count = slotCount(player, sourceId);
            for (int slot = 0; slot < count; slot++) {
                ItemStack stack = peek(player, sourceId, slot);
                if (stack.isEmpty()) {
                    continue;
                }
                if (dev.imagio.slot.inventory.core.ItemIdentityMatcher.matchesMovable(stack, identity)) {
                    return Optional.of(new CarriedSourceAccess.CarriedLocation(sourceId, slot));
                }
            }
        }
        return Optional.empty();
    }

    /** Like {@link #findIdentity} but returns every match in stable order. */
    default List<CarriedSourceAccess.CarriedLocation> findAllMatching(Player player, ItemIdentity identity) {
        if (identity == null) {
            return List.of();
        }
        java.util.ArrayList<CarriedSourceAccess.CarriedLocation> hits = new java.util.ArrayList<>();
        for (String sourceId : sourceIds(player)) {
            int count = slotCount(player, sourceId);
            for (int slot = 0; slot < count; slot++) {
                ItemStack stack = peek(player, sourceId, slot);
                if (stack.isEmpty()) {
                    continue;
                }
                if (dev.imagio.slot.inventory.core.ItemIdentityMatcher.matchesMovable(stack, identity)) {
                    hits.add(new CarriedSourceAccess.CarriedLocation(sourceId, slot));
                }
            }
        }
        return List.copyOf(hits);
    }

    /**
     * Slot capacity of the given source, or 0 if unknown / source not found.
     * Used by default {@link #findIdentity} / {@link #findAllMatching}.
     */
    int slotCount(Player player, String sourceId);

    /**
     * Fast occupied-slot summary for this provider's carried sources. The
     * default uses the read SPI, but providers with snapshot APIs should
     * override this so hot pickup-pressure checks do not re-enumerate storage
     * once per slot.
     */
    default CarriedSourceAccess.CarriedStoragePressure carriedStoragePressure(Player player) {
        int capacity = 0;
        int occupied = 0;
        for (String sourceId : sourceIds(player)) {
            int slotCount = slotCount(player, sourceId);
            capacity += Math.max(0, slotCount);
            for (int slot = 0; slot < slotCount; slot++) {
                ItemStack stack = peek(player, sourceId, slot);
                if (stack != null && !stack.isEmpty()) {
                    occupied++;
                }
            }
        }
        return new CarriedSourceAccess.CarriedStoragePressure(capacity, occupied);
    }

    /**
     * Should {@link DefaultCarriedProviderIntegration} auto-generate a
     * {@code PlayerInventoryExtension} for this provider? Defaults to
     * {@code true} — minimal providers get snapshot + mutate routing for
     * free. Providers that register a custom
     * {@code InventoryIntegrationProvider} handling the same source ids
     * must return {@code false} to avoid double-registering sources
     * (symptoms: duplicate entries in {@code carriedSources()}, ambiguous
     * mutation routing).
     */
    default boolean autoSynthesizeExtension() {
        return true;
    }
}
