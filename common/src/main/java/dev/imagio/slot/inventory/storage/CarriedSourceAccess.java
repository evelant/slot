package dev.imagio.slot.inventory.storage;

import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.SlotResourceCollections;
import dev.imagio.slot.inventory.core.SlotResourceIdentity;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Platform-neutral abstraction for reading and mutating every kind of carried
 * storage — vanilla main / hotbar / offhand / armor, Sophisticated Backpacks,
 * future curios slots, future travelers backpacks, and anything else that a
 * provider registers as a carried source.
 *
 * <p>Any code that needs to find an item identity, extract from a specific slot,
 * or insert a stack into the player's carried storage MUST go through this
 * interface. Never iterate a hardcoded list of
 * {@code {PLAYER_MAIN, PLAYER_QUICK_ACCESS_LANE_0, PLAYER_OFFHAND}} — that
 * silently excludes backpacks (and every future carried provider), which is
 * the source of the "it ignores one kind of storage" bug pattern.
 *
 * <p>Implementations are installed per platform (see
 * {@link StorageAccessRegistry#installCarriedSourceAccess}) and retrieved via
 * {@link StorageAccessRegistry#carriedSourceAccess()}.
 */
public interface CarriedSourceAccess {

    /**
     * Locator for a specific slot in a specific carried source. Returned by
     * {@link #findIdentity} so callers can pass the result back to
     * {@link #extract} or {@link #peek}.
     */
    record CarriedLocation(String sourceId, int slotIndex) {
        public CarriedLocation {
            sourceId = sourceId == null ? "" : sourceId;
        }
    }

    record CarriedStoragePressure(int slotCapacity, int occupiedSlots) {
        public CarriedStoragePressure {
            slotCapacity = Math.max(0, slotCapacity);
            occupiedSlots = Math.max(0, occupiedSlots);
        }

        public static CarriedStoragePressure empty() {
            return new CarriedStoragePressure(0, 0);
        }

        public boolean isOverThreshold(int numerator, int denominator) {
            return slotCapacity > 0
                    && denominator > 0
                    && occupiedSlots * denominator > slotCapacity * numerator;
        }

        public int slotsToFreeForThreshold(int numerator, int denominator) {
            if (slotCapacity <= 0 || denominator <= 0) {
                return 0;
            }
            int targetOccupied = Math.floorDiv(slotCapacity * numerator, denominator);
            return Math.max(0, occupiedSlots - targetOccupied);
        }
    }

    /** Read-only fluid content held by an item stack in carried storage. */
    record CarriedFluidContent(
            String sourceId,
            int slotIndex,
            int tankIndex,
            SlotResourceIdentity identity,
            long amount,
            String label
    ) {
        public CarriedFluidContent {
            sourceId = sourceId == null ? "" : sourceId;
            slotIndex = Math.max(0, slotIndex);
            tankIndex = Math.max(0, tankIndex);
            amount = Math.max(0L, amount);
            label = label == null || label.isBlank()
                    ? identity == null ? "Fluid" : identity.id()
                    : label.trim();
        }

        public boolean present() {
            return !sourceId.isBlank() && identity != null && identity.fluid() && amount > 0L;
        }
    }

    /**
     * Return a snapshot of the stack at the given carried slot, or
     * {@link ItemStack#EMPTY} if the slot is empty / the source is unknown.
     * Non-mutating.
     */
    ItemStack peek(ServerPlayer player, String sourceId, int slotIndex);

    /**
     * Extract up to {@code amount} items from the given carried slot.
     * Returns the extracted stack (may be smaller than requested). When
     * {@code simulate} is true, state is not mutated.
     */
    ItemStack extract(ServerPlayer player, String sourceId, int slotIndex, int amount, boolean simulate);

    /**
     * Insert a stack into the player's carried storage using the project's
     * {@code stableOrder} preference (backpack-first, then main, then hotbar,
     * then armor, then offhand). Returns the remainder that could not fit.
     * When {@code simulate} is true, state is not mutated.
     */
    ItemStack insertBestFit(ServerPlayer player, ItemStack stack, boolean simulate);

    /**
     * Insert into {@link CarriedProvider}-backed sources only — bypasses
     * vanilla lanes entirely. Used by pickup routing, which wants to push
     * items OUT of main/hotbar/offhand INTO backpacks/curios/etc. Returns
     * the remainder that did not fit.
     */
    ItemStack insertIntoProviders(ServerPlayer player, ItemStack stack, boolean simulate);

    /**
     * Find the first carried slot containing a stack matching the given
     * identity. Walks {@link InventoryAuthoritySnapshot#carriedSources()} in
     * {@code stableOrder}: provider-backed overflow storage first, then
     * main/hotbar/offhand.
     */
    Optional<CarriedLocation> findIdentity(ServerPlayer player, ItemIdentity identity);

    /**
     * Same as {@link #findIdentity} but returns every location, in
     * {@code stableOrder}. Used by bulk operations (deposit-all, take-all)
     * that need to walk every copy.
     */
    java.util.List<CarriedLocation> findAllMatching(ServerPlayer player, ItemIdentity identity);

    /**
     * A view of the authority snapshot scoped to the given player. Useful for
     * higher-level planners that need to enumerate every carried source /
     * every slot without dispatching reads themselves.
     */
    InventoryAuthoritySnapshot currentAuthority(ServerPlayer player);

    /**
     * Enumerates fluids contained inside carried item stacks using the active
     * platform's real fluid-container APIs. Read-only for v1; callers must not
     * treat these as item mutation locations.
     */
    default java.util.List<CarriedFluidContent> enumerateFluids(ServerPlayer player) {
        return java.util.List.of();
    }

    static Map<SlotResourceIdentity, Long> fluidCounts(java.util.List<CarriedFluidContent> contents) {
        if (contents == null || contents.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<SlotResourceIdentity, Long> counts = new LinkedHashMap<>();
        for (CarriedFluidContent content : contents) {
            if (content != null && content.present()) {
                SlotResourceCollections.mergeAmount(counts, content.identity(), content.amount());
            }
        }
        return SlotResourceCollections.normalizeAmounts(counts);
    }

    /**
     * Fast fullness summary over the same unified carried storage represented
     * by {@link InventoryAuthoritySnapshot#carriedSources()}. Platform
     * implementations may override this to avoid constructing a full authority
     * snapshot on hot paths such as junk-pickup pressure relief.
     */
    default CarriedStoragePressure carriedStoragePressure(ServerPlayer player) {
        InventoryAuthoritySnapshot authority = currentAuthority(player);
        if (authority == null) {
            return CarriedStoragePressure.empty();
        }
        int capacity = 0;
        int occupied = 0;
        for (InventorySourceDescriptor source : authority.carriedSources()) {
            if (source == null) {
                continue;
            }
            capacity += Math.max(0, authority.slotCapacity(source.id()));
            for (InventoryEntrySnapshot entry : authority.entries(source.id())) {
                if (entry != null && entry.present()) {
                    occupied++;
                }
            }
        }
        return new CarriedStoragePressure(capacity, occupied);
    }
}
