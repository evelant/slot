package dev.imagio.slot.inventory.storage;

import dev.imagio.slot.inventory.core.InventoryBindingRoute;
import dev.imagio.slot.inventory.core.InventoryCapability;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.InventorySourceDomain;
import dev.imagio.slot.inventory.core.InventorySourceRole;
import dev.imagio.slot.inventory.core.InventoryPaneMembership;
import dev.imagio.slot.inventory.core.InventoryActionRoute;
import dev.imagio.slot.inventory.core.InventoryStackSnapshot;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.integration.InventoryHostContext;
import dev.imagio.slot.inventory.integration.InventoryHostSession;
import dev.imagio.slot.inventory.integration.InventoryIntegrationProvider;
import dev.imagio.slot.inventory.integration.InventoryMutationKind;
import dev.imagio.slot.inventory.integration.InventoryMutationMode;
import dev.imagio.slot.inventory.integration.InventoryMutationRequest;
import dev.imagio.slot.inventory.integration.MutationResult;
import dev.imagio.slot.inventory.integration.PlayerInventoryContext;
import dev.imagio.slot.inventory.integration.PlayerInventoryExtension;
import dev.imagio.slot.registry.ProviderResult;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Auto-synthesised {@link InventoryIntegrationProvider} that bridges
 * {@link CarriedProvider}s into the authority-snapshot / mutation plumbing.
 * For each provider in {@link CarriedProviderRegistry} whose
 * {@link CarriedProvider#autoSynthesizeExtension()} returns {@code true},
 * this emits one {@link PlayerInventoryExtension} whose
 * {@code additionalSources()} maps {@code provider.sourceIds(player)} to
 * carried descriptors and whose {@code mutate()} routes INSERT / EXTRACT
 * back to {@link CarriedProvider#insert} / {@link CarriedProvider#extract}.
 *
 * <p>Result: a minimal carried mod only needs to implement
 * {@link CarriedProvider} and register it — the snapshot side is free.
 * Rich providers (Sophisticated Backpacks with its openable menu + tool
 * upgrades) opt out and ship their own {@link InventoryIntegrationProvider}.
 */
public final class DefaultCarriedProviderIntegration implements InventoryIntegrationProvider {

    public static final String PROVIDER_ID = "slot:default_carried_providers";

    @Override
    public String providerId() {
        return PROVIDER_ID;
    }

    @Override
    public int priority() {
        // Run late: bespoke providers (SB, tom's storage, menu-backed) should
        // resolve host sessions first. We never claim host sessions anyway.
        return -100;
    }

    @Override
    public ProviderResult<InventoryHostSession> openHost(InventoryHostContext context) {
        return ProviderResult.unsupported(
                providerId(),
                "host_sessions_not_supported",
                "DefaultCarriedProviderIntegration only auto-synthesises player extensions"
        );
    }

    @Override
    public List<PlayerInventoryExtension> playerExtensions(PlayerInventoryContext context) {
        if (context == null || context.playerInventory() == null) {
            return List.of();
        }
        Player player = context.playerInventory().player;
        if (player == null) {
            return List.of();
        }
        List<CarriedProvider> providers = CarriedProviderRegistry.all();
        if (providers.isEmpty()) {
            return List.of();
        }
        List<PlayerInventoryExtension> extensions = new ArrayList<>();
        int providerOrdinal = 0;
        for (CarriedProvider provider : providers) {
            if (!provider.autoSynthesizeExtension()) {
                providerOrdinal++;
                continue;
            }
            List<String> sourceIds;
            try {
                sourceIds = provider.sourceIds(player);
            } catch (RuntimeException | LinkageError failure) {
                sourceIds = List.of();
            }
            if (sourceIds == null) {
                sourceIds = List.of();
            }
            extensions.add(new AutoExtension(provider, player, List.copyOf(sourceIds), providerOrdinal));
            providerOrdinal++;
        }
        return List.copyOf(extensions);
    }

    private static final class AutoExtension implements PlayerInventoryExtension {
        private final CarriedProvider provider;
        private final Player capturedPlayer;
        private final List<String> sourceIds;
        private final int providerOrdinal;

        AutoExtension(CarriedProvider provider, Player capturedPlayer, List<String> sourceIds, int providerOrdinal) {
            this.provider = provider;
            this.capturedPlayer = capturedPlayer;
            this.sourceIds = sourceIds;
            this.providerOrdinal = providerOrdinal;
        }

        @Override
        public String providerId() {
            return "slot:auto/" + provider.prefix();
        }

        @Override
        public List<InventorySourceDescriptor> additionalSources() {
            if (sourceIds.isEmpty()) {
                return List.of();
            }
            ArrayList<InventorySourceDescriptor> out = new ArrayList<>(sourceIds.size());
            int perSourceOrdinal = 0;
            for (String sourceId : sourceIds) {
                int logicalSlotCount = safeSlotCount(sourceId);
                out.add(InventorySourceDescriptor.builder(sourceId)
                        .label(Component.literal(humanLabel(sourceId)))
                        .domain(InventorySourceDomain.PLAYER_EXTENSION)
                        .role(InventorySourceRole.PROVIDER_DEFINED)
                        .logicalSlotCount(logicalSlotCount)
                        .bindingRoute(InventoryBindingRoute.PROVIDER)
                        .capabilities(safeCapabilities(sourceId))
                        .actionRoute(InventoryActionRoute.PROVIDER_MUTATION)
                        .paneMembership(InventoryPaneMembership.CARRIED)
                        .diagnostics("auto/" + provider.prefix())
                        // Auto-synthesised sources rank after bespoke ones
                        // (SB carried starts at 15). 100 leaves room for
                        // future bespoke providers to claim 16..99.
                        .stableOrder(100 + providerOrdinal * 100 + perSourceOrdinal)
                        .build());
                perSourceOrdinal++;
            }
            return List.copyOf(out);
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
            return safeSlotCount(sourceId);
        }

        @Override
        public int serverSlotCapacity(ServerPlayer player, InventoryHostDescriptor host, String sourceId) {
            return safeSlotCount(sourceId);
        }

        @Override
        public MutationResult mutate(
                InventoryHostDescriptor host,
                InventoryMutationRequest request,
                InventoryMutationMode mode
        ) {
            if (request == null || request.player() == null) {
                return MutationResult.blocked("unsupported_source",
                        request == null ? ItemStack.EMPTY : request.stack());
            }
            ServerPlayer serverPlayer = request.player();
            String sourceId = request.sourceId();
            if (sourceId == null || sourceId.isBlank() || !provider.handles(sourceId)) {
                return MutationResult.blocked("unsupported_source", request.stack());
            }
            boolean simulate = mode == InventoryMutationMode.SIMULATE;
            return switch (request.kind()) {
                case INSERT -> mutateInsert(serverPlayer, sourceId, request, simulate);
                case EXTRACT -> mutateExtract(serverPlayer, sourceId, request, simulate);
                case ACTIVATE_TARGET, UNSPECIFIED ->
                        MutationResult.blocked("unsupported_mutation", request.stack());
            };
        }

        private MutationResult mutateInsert(
                ServerPlayer player,
                String sourceId,
                InventoryMutationRequest request,
                boolean simulate
        ) {
            ItemStack stack = request.stack();
            if (stack == null || stack.isEmpty()) {
                return MutationResult.success(ItemStack.EMPTY);
            }
            ItemStack remainder;
            try {
                remainder = provider.insert(player, sourceId, stack, simulate);
            } catch (RuntimeException | LinkageError failure) {
                return MutationResult.blocked("provider_insert_failed", stack);
            }
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

        private MutationResult mutateExtract(
                ServerPlayer player,
                String sourceId,
                InventoryMutationRequest request,
                boolean simulate
        ) {
            int slotIndex = request.slotIndex();
            if (slotIndex >= 0) {
                return extractFromSlot(player, sourceId, slotIndex, request, simulate);
            }
            if (request.identity() == null) {
                return MutationResult.blocked("missing_identity", ItemStack.EMPTY);
            }
            // Identity-based: walk this provider's sources, extract from the
            // first matching slot.
            List<String> sources;
            try {
                sources = provider.sourceIds(player);
            } catch (RuntimeException | LinkageError ignored) {
                sources = List.of();
            }
            if (sources == null) {
                sources = List.of();
            }
            for (String sid : sources) {
                int count = safeSlotCount(sid);
                for (int slot = 0; slot < count; slot++) {
                    ItemStack here;
                    try {
                        here = provider.peek(player, sid, slot);
                    } catch (RuntimeException | LinkageError ignored) {
                        here = ItemStack.EMPTY;
                    }
                    if (here == null || here.isEmpty()) {
                        continue;
                    }
                    if (!ItemIdentityMatcher.matchesMovable(here, request.identity())) {
                        continue;
                    }
                    int amount = request.requestedAmount(here.getCount());
                    ItemStack extracted;
                    try {
                        extracted = provider.extract(player, sid, slot, amount, simulate);
                    } catch (RuntimeException | LinkageError ignored) {
                        extracted = ItemStack.EMPTY;
                    }
                    if (extracted != null && !extracted.isEmpty()) {
                        return MutationResult.success(extracted);
                    }
                }
            }
            return MutationResult.blocked("no_matching_stack", ItemStack.EMPTY);
        }

        private MutationResult extractFromSlot(
                ServerPlayer player,
                String sourceId,
                int slotIndex,
                InventoryMutationRequest request,
                boolean simulate
        ) {
            ItemStack preview;
            try {
                preview = provider.peek(player, sourceId, slotIndex);
            } catch (RuntimeException | LinkageError ignored) {
                preview = ItemStack.EMPTY;
            }
            if (preview == null || preview.isEmpty()) {
                return MutationResult.blocked("no_matching_stack", ItemStack.EMPTY);
            }
            if (request.identity() != null
                    && !ItemIdentityMatcher.matchesMovable(preview, request.identity())) {
                return MutationResult.blocked("no_matching_stack", ItemStack.EMPTY);
            }
            int amount = request.requestedAmount(preview.getCount());
            ItemStack extracted;
            try {
                extracted = provider.extract(player, sourceId, slotIndex, amount, simulate);
            } catch (RuntimeException | LinkageError ignored) {
                extracted = ItemStack.EMPTY;
            }
            if (extracted == null || extracted.isEmpty()) {
                return MutationResult.blocked("no_matching_stack", ItemStack.EMPTY);
            }
            return MutationResult.success(extracted);
        }

        private List<InventoryStackSnapshot> readSnapshotsShared(Player player, String sourceId) {
            if (player == null || sourceId == null || !provider.handles(sourceId)) {
                return List.of();
            }
            int count = safeSlotCount(sourceId);
            if (count <= 0) {
                return List.of();
            }
            ArrayList<InventoryStackSnapshot> snapshots = new ArrayList<>();
            for (int slot = 0; slot < count; slot++) {
                ItemStack stack;
                try {
                    stack = provider.peek(player, sourceId, slot);
                } catch (RuntimeException | LinkageError ignored) {
                    stack = ItemStack.EMPTY;
                }
                if (stack == null || stack.isEmpty()) {
                    continue;
                }
                snapshots.add(new InventoryStackSnapshot(slot, stack.copy(), stack.getCount()));
            }
            return List.copyOf(snapshots);
        }

        private int safeSlotCount(String sourceId) {
            try {
                return Math.max(0, provider.slotCount(capturedPlayer, sourceId));
            } catch (RuntimeException | LinkageError ignored) {
                return 0;
            }
        }

        private Set<InventoryCapability> safeCapabilities(String sourceId) {
            try {
                Set<InventoryCapability> capabilities = provider.capabilities(capturedPlayer, sourceId);
                return capabilities == null ? Set.of() : Set.copyOf(capabilities);
            } catch (RuntimeException | LinkageError ignored) {
                return Set.of();
            }
        }

        private String humanLabel(String sourceId) {
            int slash = sourceId.indexOf('/', provider.prefix().length());
            String suffix = slash < 0 || slash + 1 >= sourceId.length()
                    ? sourceId
                    : sourceId.substring(slash + 1);
            if (suffix.length() > 8) {
                suffix = suffix.substring(0, 8);
            }
            return provider.prefix() + ":" + suffix;
        }
    }
}
