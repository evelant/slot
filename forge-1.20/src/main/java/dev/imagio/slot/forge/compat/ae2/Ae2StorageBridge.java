package dev.imagio.slot.forge.compat.ae2;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.integration.InventoryMutationMode;
import dev.imagio.slot.inventory.integration.InventoryTransferMode;
import dev.imagio.slot.inventory.query.InventoryEntryKey;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.inventory.query.InventorySourceSnapshot;
import dev.imagio.slot.inventory.storage.WorldDisplayStorageSource;
import dev.imagio.slot.inventory.storage.WorldStorageAccess;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

final class Ae2StorageBridge {
    static final String PROVIDER_ID = "ae2_terminal";
    static final String PRIMARY_SOURCE_ID = "ae2:terminal";

    private static final String ITEM_TERMINAL_PART = "appeng.parts.reporting.ItemTerminalPart";
    private static final String CRAFTING_TERMINAL_PART = "appeng.parts.reporting.CraftingTerminalPart";
    private static final String WIRELESS_TERMINAL_HOST = "appeng.helpers.WirelessTerminalMenuHost";
    private static final String WIRELESS_CRAFTING_TERMINAL_HOST = "appeng.helpers.WirelessCraftingTerminalMenuHost";
    private static final StorageBusReflection STORAGE_BUS_REFLECTION = StorageBusReflection.load();
    private static boolean storageBusReflectionWarned;

    private Ae2StorageBridge() {
    }

    static boolean isSupportedPhysicalTerminalHost(Object host) {
        if (!(host instanceof ITerminalHost) || !(host instanceof IActionHost)) {
            return false;
        }
        String className = host.getClass().getName();
        return ITEM_TERMINAL_PART.equals(className) || CRAFTING_TERMINAL_PART.equals(className);
    }

    static boolean isSupportedOpenTerminalHost(Object host) {
        if (!(host instanceof ITerminalHost) || !(host instanceof IActionHost)) {
            return false;
        }
        String className = host.getClass().getName();
        return ITEM_TERMINAL_PART.equals(className)
                || CRAFTING_TERMINAL_PART.equals(className)
                || WIRELESS_TERMINAL_HOST.equals(className)
                || WIRELESS_CRAFTING_TERMINAL_HOST.equals(className);
    }

    static Optional<Endpoint> endpoint(Object host) {
        if (!isSupportedOpenTerminalHost(host)) {
            return Optional.empty();
        }
        return endpointUnchecked(host);
    }

    static Optional<Endpoint> physicalEndpoint(Object host) {
        if (!isSupportedPhysicalTerminalHost(host)) {
            return Optional.empty();
        }
        return endpointUnchecked(host);
    }

    private static Optional<Endpoint> endpointUnchecked(Object host) {
        ITerminalHost terminalHost = (ITerminalHost) host;
        IActionHost actionHost = (IActionHost) host;
        MEStorage storage = terminalHost.getInventory();
        IGridNode node = actionHost.getActionableNode();
        if (storage == null || node == null || !node.isActive()) {
            return Optional.empty();
        }
        IGrid grid = node.getGrid();
        if (grid == null) {
            return Optional.empty();
        }
        IEnergySource energy = terminalHost instanceof IEnergySource terminalEnergy
                ? terminalEnergy
                : grid.getEnergyService();
        if (energy == null) {
            return Optional.empty();
        }
        IEnergySource activeEnergy = terminalHost instanceof IEnergySource
                ? energy
                : new ActiveGridEnergySource(node, energy);
        return Optional.of(new Endpoint(terminalHost, actionHost, node, grid, storage, activeEnergy));
    }

    static InventorySourceSnapshot sourceSnapshot(String sourceId, Endpoint endpoint) {
        if (sourceId == null || sourceId.isBlank()) {
            sourceId = PRIMARY_SOURCE_ID;
        }
        if (endpoint == null) {
            return InventorySourceSnapshot.empty(sourceId);
        }
        ArrayList<InventoryEntrySnapshot> entries = new ArrayList<>();
        for (StoredItem stored : storedItems(endpoint)) {
            entries.add(new InventoryEntrySnapshot(
                    InventoryEntryKey.providerEntry(sourceId, providerEntryId(stored.displayStack())),
                    stored.displayStack(),
                    stored.count(),
                    ""));
        }
        return new InventorySourceSnapshot(sourceId, entries.size(), List.copyOf(entries), "");
    }

    static List<WorldStorageAccess.SlotContent> slotContents(Endpoint endpoint) {
        if (endpoint == null) {
            return List.of();
        }
        ArrayList<WorldStorageAccess.SlotContent> contents = new ArrayList<>();
        int slot = 0;
        for (StoredItem stored : storedItems(endpoint)) {
            contents.add(new WorldStorageAccess.SlotContent(slot++, stored.displayStack(), stored.count()));
        }
        return contents.isEmpty() ? List.of() : List.copyOf(contents);
    }

    static List<WorldDisplayStorageSource.AliasedBlock> storageBusTargetBlocks(Endpoint endpoint) {
        if (endpoint == null || endpoint.grid() == null || !STORAGE_BUS_REFLECTION.available()) {
            return List.of();
        }
        ArrayList<WorldDisplayStorageSource.AliasedBlock> aliases = new ArrayList<>();
        try {
            @SuppressWarnings({"rawtypes", "unchecked"})
            Set<?> storageBuses = endpoint.grid().getActiveMachines((Class) STORAGE_BUS_REFLECTION.storageBusClass());
            for (Object bus : storageBuses) {
                WorldDisplayStorageSource.AliasedBlock alias = STORAGE_BUS_REFLECTION.targetBlock(bus);
                if (alias != null) {
                    aliases.add(alias);
                }
            }
        } catch (ReflectiveOperationException | RuntimeException exception) {
            warnStorageBusReflection(exception);
            return List.of();
        }
        return aliases.isEmpty() ? List.of() : List.copyOf(aliases);
    }

    static int slotCount(Endpoint endpoint) {
        return endpoint == null ? 0 : slotContents(endpoint).size();
    }

    static ItemStack insert(Endpoint endpoint, ServerPlayer player, ItemStack stack, InventoryMutationMode mode) {
        if (endpoint == null || player == null || stack == null || stack.isEmpty()) {
            return stack == null ? ItemStack.EMPTY : stack;
        }
        AEItemKey key = AEItemKey.of(stack);
        if (key == null) {
            return stack;
        }
        long inserted = StorageHelper.poweredInsert(
                endpoint.energy(),
                endpoint.storage(),
                key,
                stack.getCount(),
                IActionSource.ofPlayer(player, endpoint.actionHost()),
                actionable(mode));
        int insertedCount = Math.min(stack.getCount(), saturated(inserted));
        if (insertedCount <= 0) {
            return stack;
        }
        ItemStack remainder = stack.copy();
        remainder.shrink(insertedCount);
        return remainder.isEmpty() ? ItemStack.EMPTY : remainder;
    }

    static ItemStack extract(
            Endpoint endpoint,
            ServerPlayer player,
            String entryId,
            ItemIdentity identity,
            int requestedCount,
            InventoryTransferMode transferMode,
            InventoryMutationMode mode
    ) {
        if (endpoint == null || player == null) {
            return ItemStack.EMPTY;
        }
        StoredItem stored = findStoredItem(endpoint, entryId, identity);
        if (stored == null) {
            return ItemStack.EMPTY;
        }
        int amount = requestedAmount(requestedCount, transferMode, stored.displayStack().getMaxStackSize());
        if (amount <= 0) {
            return ItemStack.EMPTY;
        }
        long extracted = StorageHelper.poweredExtraction(
                endpoint.energy(),
                endpoint.storage(),
                stored.key(),
                amount,
                IActionSource.ofPlayer(player, endpoint.actionHost()),
                actionable(mode));
        int extractedCount = saturated(Math.min(extracted, amount));
        return extractedCount <= 0 ? ItemStack.EMPTY : stored.key().toStack(extractedCount);
    }

    static ItemStack extractSlot(
            Endpoint endpoint,
            ServerPlayer player,
            int slotIndex,
            int amount,
            boolean simulate
    ) {
        if (endpoint == null || player == null || slotIndex < 0 || amount <= 0) {
            return ItemStack.EMPTY;
        }
        List<StoredItem> items = storedItems(endpoint);
        if (slotIndex >= items.size()) {
            return ItemStack.EMPTY;
        }
        StoredItem stored = items.get(slotIndex);
        int requested = Math.min(Math.max(1, amount), stored.displayStack().getMaxStackSize());
        long extracted = StorageHelper.poweredExtraction(
                endpoint.energy(),
                endpoint.storage(),
                stored.key(),
                requested,
                IActionSource.ofPlayer(player, endpoint.actionHost()),
                simulate ? Actionable.SIMULATE : Actionable.MODULATE);
        int extractedCount = saturated(Math.min(extracted, requested));
        return extractedCount <= 0 ? ItemStack.EMPTY : stored.key().toStack(extractedCount);
    }

    private static StoredItem findStoredItem(Endpoint endpoint, String entryId, ItemIdentity identity) {
        for (StoredItem stored : storedItems(endpoint)) {
            if (entryId != null && !entryId.isBlank() && entryId.equals(providerEntryId(stored.displayStack()))) {
                return stored;
            }
            if ((entryId == null || entryId.isBlank())
                    && identity != null
                    && ItemIdentityMatcher.matchesMovable(stored.displayStack(), identity)) {
                return stored;
            }
        }
        return null;
    }

    private static List<StoredItem> storedItems(Endpoint endpoint) {
        if (endpoint == null) {
            return List.of();
        }
        KeyCounter available = endpoint.storage().getAvailableStacks();
        ArrayList<StoredItem> items = new ArrayList<>();
        for (Object2LongMap.Entry<AEKey> entry : available) {
            if (!(entry.getKey() instanceof AEItemKey key)) {
                continue;
            }
            int count = saturated(entry.getLongValue());
            if (count <= 0) {
                continue;
            }
            int displayCount = Math.min(count, Math.max(1, key.getReadOnlyStack().getMaxStackSize()));
            items.add(new StoredItem(key, key.toStack(displayCount), count));
        }
        return items.isEmpty() ? List.of() : List.copyOf(items);
    }

    private static String providerEntryId(ItemStack stack) {
        ItemIdentity identity = stack == null || stack.isEmpty() ? null : ItemIdentityMatcher.create(stack);
        if (identity == null) {
            return "";
        }
        return identity.itemId() + "|" + identity.componentFingerprint();
    }

    private static int requestedAmount(int requestedCount, InventoryTransferMode transferMode, int maxStackSize) {
        int cap = Math.max(1, maxStackSize);
        if (requestedCount > 0) {
            return Math.min(requestedCount, cap);
        }
        return switch (transferMode == null ? InventoryTransferMode.ONE : transferMode) {
            case ONE -> 1;
            case STACK, ALL -> cap;
        };
    }

    private static Actionable actionable(InventoryMutationMode mode) {
        return mode == InventoryMutationMode.SIMULATE ? Actionable.SIMULATE : Actionable.MODULATE;
    }

    private static int saturated(long value) {
        if (value <= 0L) {
            return 0;
        }
        return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
    }

    record Endpoint(
            ITerminalHost terminalHost,
            IActionHost actionHost,
            IGridNode node,
            IGrid grid,
            MEStorage storage,
            IEnergySource energy
    ) {
    }

    private record StoredItem(AEItemKey key, ItemStack displayStack, int count) {
    }

    private record ActiveGridEnergySource(IGridNode node, IEnergySource delegate) implements IEnergySource {
        @Override
        public double extractAEPower(double amt, Actionable mode, PowerMultiplier usePowerMultiplier) {
            if (node == null || !node.isActive() || delegate == null) {
                return 0.0D;
            }
            return delegate.extractAEPower(amt, mode, usePowerMultiplier);
        }
    }

    private static void warnStorageBusReflection(Exception exception) {
        if (storageBusReflectionWarned) {
            return;
        }
        storageBusReflectionWarned = true;
        SlotCommon.LOGGER.warn(
                "[SLOT] AE2 storage-bus alias detection disabled: {}",
                exception == null ? "unknown_error" : exception.getMessage());
    }

    private record StorageBusReflection(
            Class<?> storageBusClass,
            Method getBlockEntity,
            Method getSide
    ) {
        static StorageBusReflection load() {
            try {
                Class<?> type = Class.forName("appeng.parts.storagebus.StorageBusPart");
                return new StorageBusReflection(
                        type,
                        type.getMethod("getBlockEntity"),
                        type.getMethod("getSide"));
            } catch (ReflectiveOperationException exception) {
                warnStorageBusReflection(exception);
                return new StorageBusReflection(null, null, null);
            }
        }

        boolean available() {
            return storageBusClass != null && getBlockEntity != null && getSide != null;
        }

        WorldDisplayStorageSource.AliasedBlock targetBlock(Object storageBus)
                throws ReflectiveOperationException {
            if (!available() || storageBus == null || !storageBusClass.isInstance(storageBus)) {
                return null;
            }
            Object blockEntityValue = getBlockEntity.invoke(storageBus);
            Object sideValue = getSide.invoke(storageBus);
            if (!(blockEntityValue instanceof BlockEntity blockEntity) || !(sideValue instanceof Direction side)) {
                return null;
            }
            Level level = blockEntity.getLevel();
            if (level == null) {
                return null;
            }
            BlockPos target = blockEntity.getBlockPos().relative(side);
            return new WorldDisplayStorageSource.AliasedBlock(
                    level.dimension().location().toString(),
                    target.getX(),
                    target.getY(),
                    target.getZ());
        }
    }
}
