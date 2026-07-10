package dev.imagio.slot.inventory.storage;

import dev.imagio.slot.inventory.core.BuiltinInventoryDescriptors;
import dev.imagio.slot.inventory.core.HostInstanceKey;
import dev.imagio.slot.inventory.core.InventoryActionRoute;
import dev.imagio.slot.inventory.core.InventoryBindingRoute;
import dev.imagio.slot.inventory.core.InventoryCapability;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventoryPaneMembership;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.InventorySourceDomain;
import dev.imagio.slot.inventory.core.InventorySourceRole;
import dev.imagio.slot.inventory.core.InventoryTopologyDescriptor;
import dev.imagio.slot.inventory.core.MenuBackedPlayerTopologyResolver;
import dev.imagio.slot.inventory.core.PlayerRuntimeStateDescriptor;
import dev.imagio.slot.inventory.query.CursorStateSnapshot;
import dev.imagio.slot.inventory.query.InventoryAuthorityReadService;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntryKey;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.inventory.query.InventorySourceSnapshot;
import dev.imagio.slot.inventory.integration.InventoryHostFamilyHint;
import dev.imagio.slot.inventory.integration.InventoryHostObservationHints;
import dev.imagio.slot.inventory.integration.InventoryHostSession;
import dev.imagio.slot.inventory.integration.InventorySlotOwnershipPosture;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Builds the live carried-authority view used by {@link CarriedSourceAccess}.
 *
 * <p>This deliberately reads provider-backed carried sources straight from
 * {@link CarriedProviderRegistry}. The broader host resolver may expose the
 * currently-open backpack as a menu-backed UI source, but bulk carried
 * mutations execute through {@link CarriedSourceAccess}; their authority
 * snapshot must therefore use source ids that {@code peek}/{@code extract}
 * can also mutate.
 */
public final class CarriedSourceAuthoritySnapshots {
    private static final String PROVIDER_ID = "slot:carried_source_access";

    private CarriedSourceAuthoritySnapshots() {
    }

    public static InventoryAuthoritySnapshot currentAuthority(ServerPlayer player, String ownerName) {
        if (player == null || player.containerMenu == null || player.getInventory() == null) {
            return InventoryAuthoritySnapshot.empty();
        }

        InventoryTopologyDescriptor topology =
                MenuBackedPlayerTopologyResolver.resolve(player.containerMenu, player.getInventory());
        List<InventorySourceDescriptor> sources = new ArrayList<>();
        sources.addAll(providerSources(player));
        sources.addAll(BuiltinInventoryDescriptors.builtInPlayerSources(topology));

        String owner = ownerName == null || ownerName.isBlank()
                ? CarriedSourceAuthoritySnapshots.class.getSimpleName()
                : ownerName;
        InventoryHostDescriptor host = new InventoryHostDescriptor(
                HostInstanceKey.of(player.containerMenu, PROVIDER_ID, player.getUUID().toString()),
                InventoryHostDescriptor.serverMenuRef(player.containerMenu),
                owner,
                Component.literal("SLOT Carried Sources"),
                player.containerMenu,
                topology,
                InventoryHostSession.empty(),
                List.of(),
                PlayerRuntimeStateDescriptor.vanilla(player.getInventory().selected),
                List.copyOf(sources),
                BuiltinInventoryDescriptors.builtInQuickAccessLanes(),
                BuiltinInventoryDescriptors.builtInEquipmentGroups(),
                List.of(),
                new InventoryHostObservationHints(
                        InventoryHostFamilyHint.CARRIED_ONLY,
                        InventorySlotOwnershipPosture.SLOT_OWNED,
                        true,
                        true,
                        Map.of("carriedSourceAccess", "true")),
                ""
        );

        LinkedHashMap<String, InventorySourceSnapshot> snapshots = new LinkedHashMap<>();
        for (InventorySourceDescriptor source : sources) {
            if (source == null) {
                continue;
            }
            InventorySourceSnapshot snapshot = source.providerBacked()
                    ? providerSnapshot(player, source.id())
                    : InventoryAuthorityReadService.serverSourceSnapshot(player, host, source.id());
            snapshots.put(source.id(), snapshot == null ? InventorySourceSnapshot.empty(source.id()) : snapshot);
        }
        return new InventoryAuthoritySnapshot(host, Map.copyOf(snapshots), CursorStateSnapshot.empty());
    }

    private static List<InventorySourceDescriptor> providerSources(ServerPlayer player) {
        ArrayList<InventorySourceDescriptor> sources = new ArrayList<>();
        LinkedHashMap<String, InventorySourceDescriptor> unique = new LinkedHashMap<>();
        int providerOrdinal = 0;
        for (CarriedProvider provider : CarriedProviderRegistry.all()) {
            List<String> sourceIds = safeSourceIds(provider, player);
            int sourceOrdinal = 0;
            for (String sourceId : sourceIds) {
                if (sourceId == null || sourceId.isBlank() || unique.containsKey(sourceId)) {
                    sourceOrdinal++;
                    continue;
                }
                unique.put(sourceId, InventorySourceDescriptor.builder(sourceId)
                        .label(Component.literal(humanLabel(sourceId)))
                        .domain(InventorySourceDomain.PLAYER_EXTENSION)
                        .role(InventorySourceRole.PROVIDER_DEFINED)
                        .logicalSlotCount(safeSlotCount(provider, player, sourceId))
                        .bindingRoute(InventoryBindingRoute.PROVIDER)
                        .capabilities(safeCapabilities(provider, player, sourceId))
                        .actionRoute(InventoryActionRoute.PROVIDER_MUTATION)
                        .paneMembership(InventoryPaneMembership.CARRIED)
                        .diagnostics("carried-provider/" + provider.prefix())
                        .stableOrder(15 + providerOrdinal * 20 + sourceOrdinal)
                        .build());
                sourceOrdinal++;
            }
            providerOrdinal++;
        }
        sources.addAll(unique.values());
        return List.copyOf(sources);
    }

    private static InventorySourceSnapshot providerSnapshot(ServerPlayer player, String sourceId) {
        CarriedProvider provider = CarriedProviderRegistry.forSource(sourceId).orElse(null);
        if (provider == null) {
            return InventorySourceSnapshot.empty(sourceId);
        }
        int slotCount = safeSlotCount(provider, player, sourceId);
        ArrayList<InventoryEntrySnapshot> entries = new ArrayList<>();
        for (int slot = 0; slot < slotCount; slot++) {
            ItemStack stack = provider.peek(player, sourceId, slot);
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            entries.add(new InventoryEntrySnapshot(
                    InventoryEntryKey.slot(sourceId, slot),
                    stack,
                    stack.getCount(),
                    ""));
        }
        return new InventorySourceSnapshot(sourceId, slotCount, List.copyOf(entries), "");
    }

    private static List<String> safeSourceIds(CarriedProvider provider, ServerPlayer player) {
        if (provider == null) {
            return List.of();
        }
        try {
            List<String> sourceIds = provider.sourceIds(player);
            return sourceIds == null ? List.of() : List.copyOf(sourceIds);
        } catch (RuntimeException | LinkageError ignored) {
            return List.of();
        }
    }

    private static int safeSlotCount(CarriedProvider provider, ServerPlayer player, String sourceId) {
        if (provider == null || sourceId == null || sourceId.isBlank()) {
            return 0;
        }
        try {
            return Math.max(0, provider.slotCount(player, sourceId));
        } catch (RuntimeException | LinkageError ignored) {
            return 0;
        }
    }

    private static Set<InventoryCapability> safeCapabilities(CarriedProvider provider, ServerPlayer player, String sourceId) {
        if (provider == null || sourceId == null || sourceId.isBlank()) {
            return Set.of();
        }
        try {
            Set<InventoryCapability> capabilities = provider.capabilities(player, sourceId);
            return capabilities == null ? Set.of() : Set.copyOf(capabilities);
        } catch (RuntimeException | LinkageError ignored) {
            return Set.of();
        }
    }

    private static String humanLabel(String sourceId) {
        if (sourceId == null || sourceId.isBlank()) {
            return "Carried storage";
        }
        int slash = sourceId.indexOf('/');
        String prefix = slash < 0 ? sourceId : sourceId.substring(0, slash);
        return switch (prefix) {
            case "sophisticatedbackpacks:carried" -> "Backpack";
            default -> "Carried storage";
        };
    }
}
