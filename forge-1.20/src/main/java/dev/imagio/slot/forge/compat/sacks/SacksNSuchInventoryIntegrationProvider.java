package dev.imagio.slot.forge.compat.sacks;

import dev.imagio.slot.inventory.core.InventoryActionRoute;
import dev.imagio.slot.inventory.core.InventoryBindingRoute;
import dev.imagio.slot.inventory.core.InventoryCapability;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventoryPaneMembership;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.InventorySourceDomain;
import dev.imagio.slot.inventory.core.InventorySourceRole;
import dev.imagio.slot.inventory.core.InventoryStackSnapshot;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.integration.InventoryHostContext;
import dev.imagio.slot.inventory.integration.InventoryHostSession;
import dev.imagio.slot.inventory.integration.InventoryIntegrationProvider;
import dev.imagio.slot.inventory.integration.InventoryMutationMode;
import dev.imagio.slot.inventory.integration.InventoryMutationRequest;
import dev.imagio.slot.inventory.integration.MutationResult;
import dev.imagio.slot.inventory.integration.PlayerInventoryContext;
import dev.imagio.slot.inventory.integration.PlayerInventoryExtension;
import dev.imagio.slot.registry.ProviderResult;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class SacksNSuchInventoryIntegrationProvider implements InventoryIntegrationProvider {
    private static final String PROVIDER_ID = "slot:sacks_n_such";

    @Override
    public String providerId() {
        return PROVIDER_ID;
    }

    @Override
    public int priority() {
        return -50;
    }

    @Override
    public ProviderResult<InventoryHostSession> openHost(InventoryHostContext context) {
        return ProviderResult.unsupported(
                providerId(),
                "host_sessions_not_supported",
                "Sacks 'N Such contributes carried player sources only"
        );
    }

    @Override
    public List<PlayerInventoryExtension> playerExtensions(PlayerInventoryContext context) {
        if (context == null || context.playerInventory() == null || !SacksNSuchSupport.isAvailable()) {
            return List.of();
        }
        Player player = context.playerInventory().player;
        if (player == null) {
            return List.of();
        }
        List<SacksNSuchSupport.ContainerSnapshot> initialSnapshots =
                SacksNSuchSupport.readPlayerContainers(player);
        if (initialSnapshots.isEmpty()) {
            return List.of();
        }
        return List.of(new Extension(initialSnapshots));
    }

    private static final class Extension implements PlayerInventoryExtension {
        private final List<SacksNSuchSupport.ContainerSnapshot> initialSnapshots;

        private Extension(List<SacksNSuchSupport.ContainerSnapshot> initialSnapshots) {
            this.initialSnapshots = List.copyOf(initialSnapshots);
        }

        @Override
        public String providerId() {
            return PROVIDER_ID;
        }

        @Override
        public List<InventorySourceDescriptor> additionalSources() {
            ArrayList<InventorySourceDescriptor> sources = new ArrayList<>(initialSnapshots.size());
            for (SacksNSuchSupport.ContainerSnapshot snapshot : initialSnapshots) {
                sources.add(InventorySourceDescriptor.builder(snapshot.sourceId())
                        .label(snapshot.label())
                        .domain(InventorySourceDomain.PLAYER_EXTENSION)
                        .role(InventorySourceRole.PROVIDER_DEFINED)
                        .logicalSlotCount(snapshot.slotCount())
                        .bindingRoute(InventoryBindingRoute.PROVIDER)
                        .capabilities(Set.of(InventoryCapability.INSERT, InventoryCapability.EXTRACT))
                        .actionRoute(InventoryActionRoute.PROVIDER_MUTATION)
                        .paneMembership(InventoryPaneMembership.CARRIED)
                        .diagnostics("sacks_n_such/" + snapshot.sourceId())
                        .stableOrder(snapshot.stableOrder())
                        .build());
            }
            return List.copyOf(sources);
        }

        @Override
        public List<InventoryStackSnapshot> readSnapshots(
                Player player,
                InventoryHostDescriptor host,
                String sourceId
        ) {
            return readSnapshotsShared(player, sourceId);
        }

        @Override
        public List<InventoryStackSnapshot> readServerSnapshots(
                ServerPlayer player,
                InventoryHostDescriptor host,
                String sourceId
        ) {
            return readSnapshotsShared(player, sourceId);
        }

        @Override
        public int slotCapacity(Player player, InventoryHostDescriptor host, String sourceId) {
            SacksNSuchSupport.ContainerSnapshot snapshot = SacksNSuchSupport.find(player, sourceId);
            return snapshot == null ? 0 : snapshot.slotCount();
        }

        @Override
        public int serverSlotCapacity(ServerPlayer player, InventoryHostDescriptor host, String sourceId) {
            SacksNSuchSupport.ContainerSnapshot snapshot = SacksNSuchSupport.find(player, sourceId);
            return snapshot == null ? 0 : snapshot.slotCount();
        }

        @Override
        public MutationResult mutate(
                InventoryHostDescriptor host,
                InventoryMutationRequest request,
                InventoryMutationMode mode
        ) {
            if (request == null || request.player() == null) {
                return MutationResult.blocked(
                        "unsupported_source",
                        request == null ? ItemStack.EMPTY : request.stack()
                );
            }
            boolean simulate = mode == InventoryMutationMode.SIMULATE;
            return switch (request.kind()) {
                case INSERT -> mutateInsert(request, simulate);
                case EXTRACT -> mutateExtract(request, simulate);
                case ACTIVATE_TARGET, UNSPECIFIED ->
                        MutationResult.blocked("unsupported_mutation", request.stack());
            };
        }

        private static MutationResult mutateInsert(InventoryMutationRequest request, boolean simulate) {
            ItemStack stack = request.stack();
            if (stack == null || stack.isEmpty()) {
                return MutationResult.success(ItemStack.EMPTY);
            }
            SacksNSuchSupport.ContainerSnapshot snapshot =
                    SacksNSuchSupport.find(request.player(), request.sourceId());
            if (snapshot == null) {
                return MutationResult.blocked("unsupported_source", stack);
            }
            ItemStack remainder = SacksNSuchSupport.insertInto(snapshot, stack, simulate);
            if (remainder == null) {
                remainder = ItemStack.EMPTY;
            }
            if (simulate) {
                return remainder.isEmpty()
                        ? MutationResult.success(ItemStack.EMPTY)
                        : MutationResult.blocked("simulation_incomplete", remainder);
            }
            return MutationResult.success(remainder);
        }

        private static MutationResult mutateExtract(InventoryMutationRequest request, boolean simulate) {
            if (request.slotIndex() >= 0) {
                return extractFromSlot(request, simulate);
            }
            if (request.identity() == null) {
                return MutationResult.blocked("missing_identity", ItemStack.EMPTY);
            }
            for (SacksNSuchSupport.ContainerSnapshot snapshot :
                    SacksNSuchSupport.readPlayerContainers(request.player())) {
                for (int slot = 0; slot < snapshot.slotCount(); slot++) {
                    ItemStack preview = SacksNSuchSupport.peek(snapshot, slot);
                    if (!ItemIdentityMatcher.matchesMovable(preview, request.identity())) {
                        continue;
                    }
                    int amount = request.requestedAmount(preview.getCount());
                    ItemStack extracted = SacksNSuchSupport.extract(snapshot, slot, amount, simulate);
                    if (extracted != null && !extracted.isEmpty()) {
                        return MutationResult.success(extracted);
                    }
                }
            }
            return MutationResult.blocked("no_matching_stack", ItemStack.EMPTY);
        }

        private static MutationResult extractFromSlot(InventoryMutationRequest request, boolean simulate) {
            SacksNSuchSupport.ContainerSnapshot snapshot =
                    SacksNSuchSupport.find(request.player(), request.sourceId());
            if (snapshot == null) {
                return MutationResult.blocked("unsupported_source", ItemStack.EMPTY);
            }
            ItemStack preview = SacksNSuchSupport.peek(snapshot, request.slotIndex());
            if (preview.isEmpty()) {
                return MutationResult.blocked("no_matching_stack", ItemStack.EMPTY);
            }
            if (request.identity() != null
                    && !ItemIdentityMatcher.matchesMovable(preview, request.identity())) {
                return MutationResult.blocked("no_matching_stack", ItemStack.EMPTY);
            }
            int amount = request.requestedAmount(preview.getCount());
            ItemStack extracted = SacksNSuchSupport.extract(snapshot, request.slotIndex(), amount, simulate);
            if (extracted == null || extracted.isEmpty()) {
                return MutationResult.blocked("no_matching_stack", ItemStack.EMPTY);
            }
            return MutationResult.success(extracted);
        }

        private static List<InventoryStackSnapshot> readSnapshotsShared(Player player, String sourceId) {
            SacksNSuchSupport.ContainerSnapshot snapshot = SacksNSuchSupport.find(player, sourceId);
            if (snapshot == null || snapshot.slotCount() <= 0) {
                return List.of();
            }
            ArrayList<InventoryStackSnapshot> snapshots = new ArrayList<>();
            for (int slot = 0; slot < snapshot.slotCount(); slot++) {
                ItemStack stack = SacksNSuchSupport.peek(snapshot, slot);
                if (stack.isEmpty()) {
                    continue;
                }
                snapshots.add(new InventoryStackSnapshot(slot, stack.copy(), stack.getCount()));
            }
            return List.copyOf(snapshots);
        }
    }
}
