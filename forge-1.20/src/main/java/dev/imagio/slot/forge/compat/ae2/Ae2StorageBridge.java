package dev.imagio.slot.forge.compat.ae2;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.implementations.blockentities.IChestOrDrive;
import appeng.api.inventories.InternalInventory;
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
import appeng.api.storage.StorageCells;
import appeng.api.storage.StorageHelper;
import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityCollections;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

final class Ae2StorageBridge {
    static final String PROVIDER_ID = "ae2_terminal";
    static final String PRIMARY_SOURCE_ID = "ae2:terminal";
    static final String NETWORK_PROVIDER_ID = "ae2";
    static final String ROUTE_TERMINAL = "terminal";
    static final String ROUTE_OPEN_TERMINAL = "open_terminal";
    private static final String NETWORK_STORAGE_PREFIX = "ae2:network:";
    private static final String SLOT_MEDIA_ID_TAG = "slot_media_id";

    private static final String ITEM_TERMINAL_PART = "appeng.parts.reporting.ItemTerminalPart";
    private static final String CRAFTING_TERMINAL_PART = "appeng.parts.reporting.CraftingTerminalPart";
    private static final String WIRELESS_TERMINAL_HOST = "appeng.helpers.WirelessTerminalMenuHost";
    private static final String WIRELESS_CRAFTING_TERMINAL_HOST = "appeng.helpers.WirelessCraftingTerminalMenuHost";
    private static final StorageBusReflection STORAGE_BUS_REFLECTION = StorageBusReflection.load();
    private static final IoPortReflection IO_PORT_REFLECTION = IoPortReflection.load();
    private static boolean storageBusReflectionWarned;
    private static boolean ioPortReflectionWarned;

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

    static Optional<NetworkSnapshot> networkSnapshot(Endpoint endpoint) {
        if (endpoint == null) {
            return Optional.empty();
        }
        NetworkObservation observation = networkObservation(endpoint);
        if (observation.mediaIds().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new NetworkSnapshot(
                NETWORK_STORAGE_PREFIX + mediaSetHash(observation.mediaIds()),
                observation.mediaIds(),
                observation.contents(),
                observation.aliasedBlocks(),
                observation.mediaObservations()));
    }

    static WorldDisplayStorageSource routedNetworkSource(
            Endpoint endpoint,
            String dimension,
            BlockPos routePos,
            boolean includeLiveOnlyFallback
    ) {
        return routedNetworkSource(endpoint, dimension, routePos, ROUTE_TERMINAL, includeLiveOnlyFallback);
    }

    static WorldDisplayStorageSource openNetworkSource(Endpoint endpoint) {
        return routedNetworkSource(endpoint, "", BlockPos.ZERO, ROUTE_OPEN_TERMINAL, false);
    }

    private static WorldDisplayStorageSource routedNetworkSource(
            Endpoint endpoint,
            String dimension,
            BlockPos routePos,
            String routeKind,
            boolean includeLiveOnlyFallback
    ) {
        if (endpoint == null || routePos == null) {
            return null;
        }
        NetworkObservation observation = networkObservation(endpoint);
        if (!observation.mediaIds().isEmpty()) {
            NetworkSnapshot snapshot = new NetworkSnapshot(
                    NETWORK_STORAGE_PREFIX + mediaSetHash(observation.mediaIds()),
                    observation.mediaIds(),
                    observation.contents(),
                    observation.aliasedBlocks(),
                    observation.mediaObservations());
            return new WorldDisplayStorageSource(
                    snapshot.storageId(),
                    dev.imagio.slot.inventory.storage.WorldDisplayStorageKind.AE2_NETWORK,
                    "ME network @ " + routePos.getX() + "," + routePos.getY() + "," + routePos.getZ(),
                    dimension,
                    routePos.getX(),
                    routePos.getY(),
                    routePos.getZ(),
                    snapshot.contents().size(),
                    snapshot.contents(),
                    List.of(),
                    snapshot.aliasedBlocks(),
                    snapshot.mediaIds(),
                    snapshot.mediaObservations(),
                    new WorldStorageAccess.Target.Virtual(
                            NETWORK_PROVIDER_ID,
                            snapshot.storageId(),
                            routeKind,
                            dimension,
                            routePos.getX(),
                            routePos.getY(),
                            routePos.getZ()));
        }
        if (!includeLiveOnlyFallback) {
            return null;
        }
        return new WorldDisplayStorageSource(
                WorldDisplayStorageSource.storageId(
                        dev.imagio.slot.inventory.storage.WorldDisplayStorageKind.AE2_TERMINAL,
                        dimension,
                        routePos.getX(),
                        routePos.getY(),
                        routePos.getZ()),
                dev.imagio.slot.inventory.storage.WorldDisplayStorageKind.AE2_TERMINAL,
                "ME network @ " + routePos.getX() + "," + routePos.getY() + "," + routePos.getZ(),
                dimension,
                routePos.getX(),
                routePos.getY(),
                routePos.getZ(),
                observation.contents().size(),
                observation.contents(),
                List.of(),
                observation.aliasedBlocks(),
                List.of(),
                observation.mediaObservations(),
                null);
    }

    static Optional<Endpoint> openMenuEndpoint(ServerPlayer player, String expectedStorageId) {
        if (player == null) {
            return Optional.empty();
        }
        AbstractContainerMenu menu = player.containerMenu;
        if (menu == null) {
            return Optional.empty();
        }
        Optional<Endpoint> endpoint = endpoint(terminalHost(menu));
        if (endpoint.isEmpty()) {
            return Optional.empty();
        }
        if (isAe2NetworkStorageId(expectedStorageId)) {
            Optional<NetworkSnapshot> network = networkSnapshot(endpoint.get());
            if (network.isEmpty() || !expectedStorageId.equals(network.get().storageId())) {
                return Optional.empty();
            }
        }
        return endpoint;
    }

    static Optional<PhysicalRoute> physicalRoute(Object host) {
        if (!isSupportedPhysicalTerminalHost(host)) {
            return Optional.empty();
        }
        try {
            Method method = host.getClass().getMethod("getBlockEntity");
            Object value = method.invoke(host);
            if (!(value instanceof BlockEntity blockEntity)) {
                return Optional.empty();
            }
            Level level = blockEntity.getLevel();
            if (level == null) {
                return Optional.empty();
            }
            return Optional.of(new PhysicalRoute(
                    level.dimension().location().toString(),
                    blockEntity.getBlockPos()));
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return Optional.empty();
        }
    }

    static Object terminalHost(AbstractContainerMenu menu) {
        if (menu == null) {
            return null;
        }
        try {
            Method method = menu.getClass().getMethod("getHost");
            return method.invoke(menu);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
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

    static boolean isAe2NetworkStorageId(String storageId) {
        return storageId != null && storageId.startsWith(NETWORK_STORAGE_PREFIX);
    }

    static boolean endpointMatchesStorageId(Endpoint endpoint, String expectedStorageId) {
        if (!isAe2NetworkStorageId(expectedStorageId)) {
            return true;
        }
        Optional<NetworkSnapshot> snapshot = networkSnapshot(endpoint);
        return snapshot.isPresent() && expectedStorageId.equals(snapshot.get().storageId());
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

    private static NetworkObservation networkObservation(Endpoint endpoint) {
        if (endpoint == null) {
            return NetworkObservation.empty();
        }
        List<WorldStorageAccess.SlotContent> contents = slotContents(endpoint);
        List<WorldDisplayStorageSource.AliasedBlock> aliases = storageBusTargetBlocks(endpoint);
        if (endpoint == null || endpoint.grid() == null) {
            return new NetworkObservation(List.of(), contents, aliases, List.of());
        }
        LinkedHashSet<String> activeMediaIds = new LinkedHashSet<>();
        ArrayList<WorldDisplayStorageSource.MediaObservation> observations = new ArrayList<>();
        LinkedHashSet<String> seenMediaIds = new LinkedHashSet<>();
        try {
            Set<IChestOrDrive> cellHosts = endpoint.grid().getActiveMachines(IChestOrDrive.class);
            for (IChestOrDrive host : cellHosts) {
                if (host == null || !host.isPowered()) {
                    continue;
                }
                InternalInventory inventory = internalInventory(host);
                if (inventory == null) {
                    continue;
                }
                int slots = Math.min(Math.max(0, host.getCellCount()), inventory.size());
                HolderRoute route = holderRoute(host);
                for (int slot = 0; slot < slots; slot++) {
                    ItemStack stack = inventory.getStackInSlot(slot);
                    if (stack == null || stack.isEmpty() || !StorageCells.isCellHandled(stack)) {
                        continue;
                    }
                    String mediaId = mediaId(inventory, slot, stack, seenMediaIds);
                    if (mediaId.isBlank()) {
                        continue;
                    }
                    MEStorage cellStorage = host.getCellInventory(slot);
                    CellObservation observation = cellObservation(
                            mediaId,
                            "drive",
                            route,
                            cellStorage == null ? StorageCells.getCellInventory(stack, null) : cellStorage);
                    observations.add(observation.toMediaObservation());
                    if (observation.activeItemStorage()) {
                        activeMediaIds.add(mediaId);
                    }
                }
            }
            observations.addAll(ioPortMediaObservations(endpoint, seenMediaIds));
        } catch (RuntimeException exception) {
            SlotCommon.LOGGER.warn(
                    "[SLOT] AE2 media identity discovery failed: {}",
                    exception.getMessage() == null ? "runtime_exception" : exception.getMessage());
            return new NetworkObservation(List.of(), contents, aliases, observations);
        }
        if (activeMediaIds.isEmpty()) {
            return new NetworkObservation(List.of(), contents, aliases, observations);
        }
        ArrayList<String> sorted = new ArrayList<>(activeMediaIds);
        sorted.sort(String::compareTo);
        return new NetworkObservation(
                List.copyOf(sorted),
                contents,
                aliases,
                observations.isEmpty() ? List.of() : List.copyOf(observations));
    }

    private static InternalInventory internalInventory(IChestOrDrive host) {
        if (host == null) {
            return null;
        }
        try {
            Method method = host.getClass().getMethod("getInternalInventory");
            Object value = method.invoke(host);
            return value instanceof InternalInventory inventory ? inventory : null;
        } catch (ReflectiveOperationException | RuntimeException exception) {
            return null;
        }
    }

    private static List<WorldDisplayStorageSource.MediaObservation> ioPortMediaObservations(
            Endpoint endpoint,
            Set<String> seenMediaIds
    ) {
        if (endpoint == null || endpoint.grid() == null || !IO_PORT_REFLECTION.available()) {
            return List.of();
        }
        ArrayList<WorldDisplayStorageSource.MediaObservation> observations = new ArrayList<>();
        try {
            @SuppressWarnings({"rawtypes", "unchecked"})
            Set<?> ports = endpoint.grid().getActiveMachines((Class) IO_PORT_REFLECTION.ioPortClass());
            for (Object port : ports) {
                InternalInventory inventory = IO_PORT_REFLECTION.internalInventory(port);
                if (inventory == null) {
                    continue;
                }
                HolderRoute route = holderRoute(port);
                for (int slot = 0; slot < inventory.size(); slot++) {
                    ItemStack stack = inventory.getStackInSlot(slot);
                    if (stack == null || stack.isEmpty() || !StorageCells.isCellHandled(stack)) {
                        continue;
                    }
                    String mediaId = mediaId(inventory, slot, stack, seenMediaIds);
                    if (mediaId.isBlank()) {
                        continue;
                    }
                    CellObservation observation = cellObservation(
                            mediaId,
                            "io_port",
                            route,
                            StorageCells.getCellInventory(stack, null));
                    observations.add(observation.toMediaObservation());
                }
            }
        } catch (ReflectiveOperationException | RuntimeException exception) {
            warnIoPortReflection(exception);
            return List.of();
        }
        return observations.isEmpty() ? List.of() : List.copyOf(observations);
    }

    private static CellObservation cellObservation(
            String mediaId,
            String holderKind,
            HolderRoute route,
            MEStorage storage
    ) {
        if (storage == null) {
            return new CellObservation(
                    mediaId,
                    WorldDisplayStorageSource.MediaObservation.STATUS_UNREADABLE,
                    holderKind,
                    route,
                    Map.of());
        }
        CellContents contents = cellContents(storage);
        String status;
        if (!contents.itemCounts().isEmpty()) {
            status = WorldDisplayStorageSource.MediaObservation.STATUS_ACTIVE;
        } else if (contents.hasNonItemKeys()) {
            status = WorldDisplayStorageSource.MediaObservation.STATUS_NON_ITEM;
        } else {
            status = WorldDisplayStorageSource.MediaObservation.STATUS_EMPTY;
        }
        return new CellObservation(mediaId, status, holderKind, route, contents.itemCounts());
    }

    private static CellContents cellContents(MEStorage storage) {
        if (storage == null) {
            return new CellContents(Map.of(), false);
        }
        LinkedHashMap<ItemIdentity, Integer> counts = new LinkedHashMap<>();
        boolean hasNonItemKeys = false;
        KeyCounter available = storage.getAvailableStacks();
        for (Object2LongMap.Entry<AEKey> entry : available) {
            if (entry == null || entry.getLongValue() <= 0) {
                continue;
            }
            if (!(entry.getKey() instanceof AEItemKey key)) {
                hasNonItemKeys = true;
                continue;
            }
            int count = saturated(entry.getLongValue());
            if (count <= 0) {
                continue;
            }
            ItemIdentity identity = ItemIdentityMatcher.normalizeMovable(
                    ItemIdentityMatcher.create(key.toStack(1)));
            if (identity != null) {
                ItemIdentityCollections.mergeCount(counts, identity, count);
            }
        }
        return new CellContents(counts.isEmpty() ? Map.of() : Map.copyOf(counts), hasNonItemKeys);
    }

    private static HolderRoute holderRoute(Object holder) {
        if (!(holder instanceof BlockEntity blockEntity)) {
            return HolderRoute.empty();
        }
        Level level = blockEntity.getLevel();
        if (level == null) {
            return HolderRoute.empty();
        }
        BlockPos pos = blockEntity.getBlockPos();
        return new HolderRoute(level.dimension().location().toString(), pos.getX(), pos.getY(), pos.getZ());
    }

    private static String mediaId(InternalInventory inventory, int slot, ItemStack stack, Set<String> seenMediaIds) {
        if (inventory == null || stack == null || stack.isEmpty()) {
            return "";
        }
        CompoundTag tag = stack.getOrCreateTag();
        String existing = tag.getString(SLOT_MEDIA_ID_TAG);
        if (existing.isBlank()) {
            String created = writeMediaId(inventory, slot, stack, UUID.randomUUID().toString());
            if (!created.isBlank() && seenMediaIds != null) {
                seenMediaIds.add(created);
            }
            return created;
        }
        if (seenMediaIds != null && !seenMediaIds.add(existing)) {
            String created = UUID.randomUUID().toString();
            SlotCommon.LOGGER.warn("[SLOT] AE2 storage cell media id duplicated; re-stamping later cell");
            String stamped = writeMediaId(inventory, slot, stack, created);
            if (!stamped.isBlank()) {
                seenMediaIds.add(stamped);
            }
            return stamped;
        }
        return existing;
    }

    private static String writeMediaId(InternalInventory inventory, int slot, ItemStack stack, String mediaId) {
        if (mediaId == null || mediaId.isBlank()) {
            return "";
        }
        try {
            ItemStack stamped = stack.copy();
            stamped.getOrCreateTag().putString(SLOT_MEDIA_ID_TAG, mediaId);
            inventory.setItemDirect(slot, stamped);
            return mediaId;
        } catch (RuntimeException exception) {
            SlotCommon.LOGGER.warn(
                    "[SLOT] AE2 storage cell media id stamp failed: {}",
                    exception.getMessage() == null ? "runtime_exception" : exception.getMessage());
            return "";
        }
    }

    private static String mediaSetHash(List<String> mediaIds) {
        ArrayList<String> sorted = new ArrayList<>(mediaIds == null ? List.of() : mediaIds);
        sorted.removeIf(id -> id == null || id.isBlank());
        sorted.sort(Comparator.naturalOrder());
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (String id : sorted) {
                digest.update(id.getBytes(StandardCharsets.UTF_8));
                digest.update((byte) 0);
            }
            byte[] hash = digest.digest();
            StringBuilder out = new StringBuilder(hash.length * 2);
            for (byte value : hash) {
                out.append(Character.forDigit((value >> 4) & 0xF, 16));
                out.append(Character.forDigit(value & 0xF, 16));
            }
            return out.toString();
        } catch (NoSuchAlgorithmException exception) {
            return Integer.toHexString(sorted.hashCode());
        }
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

    record NetworkObservation(
            List<String> mediaIds,
            List<WorldStorageAccess.SlotContent> contents,
            List<WorldDisplayStorageSource.AliasedBlock> aliasedBlocks,
            List<WorldDisplayStorageSource.MediaObservation> mediaObservations
    ) {
        NetworkObservation {
            mediaIds = mediaIds == null ? List.of() : List.copyOf(mediaIds);
            contents = contents == null ? List.of() : List.copyOf(contents);
            aliasedBlocks = aliasedBlocks == null ? List.of() : List.copyOf(aliasedBlocks);
            mediaObservations = mediaObservations == null ? List.of() : List.copyOf(mediaObservations);
        }

        static NetworkObservation empty() {
            return new NetworkObservation(List.of(), List.of(), List.of(), List.of());
        }
    }

    record NetworkSnapshot(
            String storageId,
            List<String> mediaIds,
            List<WorldStorageAccess.SlotContent> contents,
            List<WorldDisplayStorageSource.AliasedBlock> aliasedBlocks,
            List<WorldDisplayStorageSource.MediaObservation> mediaObservations
    ) {
        NetworkSnapshot {
            storageId = storageId == null ? "" : storageId;
            mediaIds = mediaIds == null ? List.of() : List.copyOf(mediaIds);
            contents = contents == null ? List.of() : List.copyOf(contents);
            aliasedBlocks = aliasedBlocks == null ? List.of() : List.copyOf(aliasedBlocks);
            mediaObservations = mediaObservations == null ? List.of() : List.copyOf(mediaObservations);
        }
    }

    private record CellContents(Map<ItemIdentity, Integer> itemCounts, boolean hasNonItemKeys) {
        private CellContents {
            itemCounts = itemCounts == null ? Map.of() : Map.copyOf(itemCounts);
        }
    }

    private record HolderRoute(String dimensionId, int x, int y, int z) {
        private HolderRoute {
            dimensionId = dimensionId == null ? "" : dimensionId;
        }

        static HolderRoute empty() {
            return new HolderRoute("", 0, 0, 0);
        }
    }

    private record CellObservation(
            String mediaId,
            String status,
            String holderKind,
            HolderRoute route,
            Map<ItemIdentity, Integer> countsByIdentity
    ) {
        private CellObservation {
            mediaId = mediaId == null ? "" : mediaId;
            status = status == null || status.isBlank()
                    ? WorldDisplayStorageSource.MediaObservation.STATUS_UNREADABLE
                    : status;
            holderKind = holderKind == null ? "" : holderKind;
            route = route == null ? HolderRoute.empty() : route;
            countsByIdentity = countsByIdentity == null ? Map.of() : Map.copyOf(countsByIdentity);
        }

        boolean activeItemStorage() {
            return WorldDisplayStorageSource.MediaObservation.STATUS_ACTIVE.equals(status)
                    && !countsByIdentity.isEmpty();
        }

        WorldDisplayStorageSource.MediaObservation toMediaObservation() {
            return new WorldDisplayStorageSource.MediaObservation(
                    mediaId,
                    status,
                    holderKind,
                    route.dimensionId(),
                    route.x(),
                    route.y(),
                    route.z(),
                    countsByIdentity);
        }
    }

    record PhysicalRoute(String dimensionId, BlockPos pos) {
        PhysicalRoute {
            dimensionId = dimensionId == null ? "" : dimensionId;
        }
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

    private static void warnIoPortReflection(Exception exception) {
        if (ioPortReflectionWarned) {
            return;
        }
        ioPortReflectionWarned = true;
        SlotCommon.LOGGER.warn(
                "[SLOT] AE2 IO port media observation disabled: {}",
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

    private record IoPortReflection(
            Class<?> ioPortClass,
            Method getInternalInventory
    ) {
        static IoPortReflection load() {
            try {
                Class<?> type = Class.forName("appeng.blockentity.storage.IOPortBlockEntity");
                return new IoPortReflection(type, type.getMethod("getInternalInventory"));
            } catch (ReflectiveOperationException exception) {
                warnIoPortReflection(exception);
                return new IoPortReflection(null, null);
            }
        }

        boolean available() {
            return ioPortClass != null && getInternalInventory != null;
        }

        InternalInventory internalInventory(Object ioPort)
                throws ReflectiveOperationException {
            if (!available() || ioPort == null || !ioPortClass.isInstance(ioPort)) {
                return null;
            }
            Object value = getInternalInventory.invoke(ioPort);
            return value instanceof InternalInventory inventory ? inventory : null;
        }
    }
}
