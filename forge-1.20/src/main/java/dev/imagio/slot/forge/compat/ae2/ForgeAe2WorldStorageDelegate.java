package dev.imagio.slot.forge.compat.ae2;

import appeng.api.parts.IPart;
import appeng.api.parts.PartHelper;
import dev.imagio.slot.inventory.storage.WorldDisplayStorageKind;
import dev.imagio.slot.inventory.storage.WorldDisplayStorageSource;
import dev.imagio.slot.inventory.storage.WorldStorageAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Optional;

public final class ForgeAe2WorldStorageDelegate implements WorldStorageAccess.Delegate {
    @Override
    public boolean matches(WorldStorageAccess.Target target) {
        if (target instanceof WorldStorageAccess.Target.Virtual virtual) {
            return Ae2StorageBridge.NETWORK_PROVIDER_ID.equals(virtual.providerId());
        }
        return target instanceof WorldStorageAccess.Target.Display display
                && (display.kind() == WorldDisplayStorageKind.AE2_TERMINAL
                || display.kind() == WorldDisplayStorageKind.AE2_NETWORK);
    }

    @Override
    public Optional<ItemStack> insert(
            ServerPlayer actor,
            MinecraftServer server,
            WorldStorageAccess.Target target,
            ItemStack stack,
            boolean simulate
    ) {
        Optional<Ae2StorageBridge.Endpoint> endpoint = resolve(actor, server, target);
        if (endpoint.isEmpty()) {
            return Optional.of(stack == null ? ItemStack.EMPTY : stack);
        }
        return Optional.of(Ae2StorageBridge.insert(
                endpoint.get(),
                actor,
                stack,
                simulate ? dev.imagio.slot.inventory.integration.InventoryMutationMode.SIMULATE
                        : dev.imagio.slot.inventory.integration.InventoryMutationMode.EXECUTE));
    }

    @Override
    public Optional<ItemStack> extract(
            ServerPlayer actor,
            MinecraftServer server,
            WorldStorageAccess.Target target,
            int slotIndex,
            int amount,
            boolean simulate
    ) {
        Optional<Ae2StorageBridge.Endpoint> endpoint = resolve(actor, server, target);
        if (endpoint.isEmpty()) {
            return Optional.of(ItemStack.EMPTY);
        }
        return Optional.of(Ae2StorageBridge.extractSlot(endpoint.get(), actor, slotIndex, amount, simulate));
    }

    @Override
    public Optional<List<WorldStorageAccess.SlotContent>> enumerate(
            MinecraftServer server,
            WorldStorageAccess.Target target
    ) {
        return resolve(server, target).map(Ae2StorageBridge::slotContents);
    }

    @Override
    public Optional<Integer> slotCount(MinecraftServer server, WorldStorageAccess.Target target) {
        return resolve(server, target).map(Ae2StorageBridge::slotCount);
    }

    @Override
    public List<WorldDisplayStorageSource> proximateDisplaySources(ServerPlayer player, int radiusBlocks) {
        if (player == null || player.getServer() == null) {
            return List.of();
        }
        int radius = Math.max(0, radiusBlocks);
        ServerLevel level = player.serverLevel();
        String dimension = level.dimension().location().toString();
        BlockPos center = player.blockPosition();
        long radiusSquared = (long) radius * radius;
        ArrayList<NetworkDisplayCandidate> candidates = new ArrayList<>();
        for (BlockPos cursor : BlockPos.betweenClosed(
                center.offset(-radius, -radius, -radius),
                center.offset(radius, radius, radius))) {
            long dx = (long) cursor.getX() - center.getX();
            long dy = (long) cursor.getY() - center.getY();
            long dz = (long) cursor.getZ() - center.getZ();
            if (dx * dx + dy * dy + dz * dz > radiusSquared || !level.isLoaded(cursor)) {
                continue;
            }
            Optional<Ae2StorageBridge.Endpoint> endpoint = resolvePartEndpoint(level, cursor);
            if (endpoint.isEmpty()) {
                continue;
            }
            Ae2StorageBridge.Endpoint resolvedEndpoint = endpoint.get();
            if (resolvedEndpoint.grid() == null) {
                continue;
            }
            WorldDisplayStorageSource source = sourceForEndpoint(
                    resolvedEndpoint,
                    dimension,
                    cursor);
            if (source == null) {
                continue;
            }
            candidates.add(new NetworkDisplayCandidate(
                    resolvedEndpoint.grid(),
                    dx * dx + dy * dy + dz * dz,
                    source));
        }
        ArrayList<WorldDisplayStorageSource> sources = new ArrayList<>(nearestSourcesByNetwork(candidates));
        sources.sort(Comparator
                .comparingLong((WorldDisplayStorageSource source) -> distanceSquared(source, center))
                .thenComparing(WorldDisplayStorageSource::storageId));
        return sources.isEmpty() ? List.of() : List.copyOf(sources);
    }

    static List<WorldDisplayStorageSource> nearestSourcesByNetwork(List<NetworkDisplayCandidate> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }
        IdentityHashMap<Object, NetworkDisplayCandidate> nearestByNetwork = new IdentityHashMap<>();
        for (NetworkDisplayCandidate candidate : candidates) {
            if (candidate == null || candidate.networkKey() == null || candidate.source() == null) {
                continue;
            }
            NetworkDisplayCandidate existing = nearestByNetwork.get(candidate.networkKey());
            if (existing == null || candidate.isPreferredTo(existing)) {
                nearestByNetwork.put(candidate.networkKey(), candidate);
            }
        }
        if (nearestByNetwork.isEmpty()) {
            return List.of();
        }
        ArrayList<WorldDisplayStorageSource> out = new ArrayList<>(nearestByNetwork.size());
        for (NetworkDisplayCandidate candidate : nearestByNetwork.values()) {
            out.add(candidate.source());
        }
        return List.copyOf(out);
    }

    private static Optional<Ae2StorageBridge.Endpoint> resolve(
            ServerPlayer actor,
            MinecraftServer server,
            WorldStorageAccess.Target target
    ) {
        if (target instanceof WorldStorageAccess.Target.Virtual virtual
                && Ae2StorageBridge.NETWORK_PROVIDER_ID.equals(virtual.providerId())
                && Ae2StorageBridge.ROUTE_OPEN_TERMINAL.equals(virtual.routeKind())) {
            return Ae2StorageBridge.openMenuEndpoint(actor, virtual.storageId());
        }
        return resolve(server, target);
    }

    private static Optional<Ae2StorageBridge.Endpoint> resolve(
            MinecraftServer server,
            WorldStorageAccess.Target target
    ) {
        if (server == null) {
            return Optional.empty();
        }
        String dimensionId;
        int x;
        int y;
        int z;
        String expectedStorageId = "";
        if (target instanceof WorldStorageAccess.Target.Virtual virtual
                && Ae2StorageBridge.NETWORK_PROVIDER_ID.equals(virtual.providerId())) {
            dimensionId = virtual.dimensionId();
            x = virtual.x();
            y = virtual.y();
            z = virtual.z();
            expectedStorageId = virtual.storageId();
        } else if (target instanceof WorldStorageAccess.Target.Display display
                && (display.kind() == WorldDisplayStorageKind.AE2_TERMINAL
                || display.kind() == WorldDisplayStorageKind.AE2_NETWORK)) {
            dimensionId = display.dimensionId();
            x = display.x();
            y = display.y();
            z = display.z();
        } else {
            return Optional.empty();
        }
        ServerLevel level = level(server, dimensionId);
        if (level == null) {
            return Optional.empty();
        }
        BlockPos pos = new BlockPos(x, y, z);
        if (!level.isLoaded(pos)) {
            return Optional.empty();
        }
        Optional<Ae2StorageBridge.Endpoint> endpoint = resolvePartEndpoint(level, pos);
        if (endpoint.isEmpty()
                || !Ae2StorageBridge.endpointMatchesStorageId(endpoint.get(), expectedStorageId)) {
            return Optional.empty();
        }
        return endpoint;
    }

    private static WorldDisplayStorageSource sourceForEndpoint(
            Ae2StorageBridge.Endpoint endpoint,
            String dimension,
            BlockPos routePos
    ) {
        return Ae2StorageBridge.routedNetworkSource(endpoint, dimension, routePos, true);
    }

    private static Optional<Ae2StorageBridge.Endpoint> resolvePartEndpoint(ServerLevel level, BlockPos pos) {
        for (Direction side : Direction.values()) {
            IPart part = PartHelper.getPart(level, pos, side);
            Optional<Ae2StorageBridge.Endpoint> endpoint = Ae2StorageBridge.physicalEndpoint(part);
            if (endpoint.isPresent()) {
                return endpoint;
            }
        }
        return Optional.empty();
    }

    private static ServerLevel level(MinecraftServer server, String dimensionId) {
        if (server == null || dimensionId == null || dimensionId.isBlank()) {
            return null;
        }
        for (ServerLevel level : server.getAllLevels()) {
            if (level.dimension().location().toString().equals(dimensionId)) {
                return level;
            }
        }
        return null;
    }

    private static long distanceSquared(WorldDisplayStorageSource source, BlockPos center) {
        long dx = (long) source.x() - center.getX();
        long dy = (long) source.y() - center.getY();
        long dz = (long) source.z() - center.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    record NetworkDisplayCandidate(
            Object networkKey,
            long distanceSquared,
            WorldDisplayStorageSource source
    ) {
        private boolean isPreferredTo(NetworkDisplayCandidate other) {
            if (other == null) {
                return true;
            }
            int distanceCompare = Long.compare(distanceSquared, other.distanceSquared);
            if (distanceCompare != 0) {
                return distanceCompare < 0;
            }
            String sourceId = source == null ? "" : source.storageId();
            String otherSourceId = other.source() == null ? "" : other.source().storageId();
            return sourceId.compareTo(otherSourceId) < 0;
        }
    }
}
