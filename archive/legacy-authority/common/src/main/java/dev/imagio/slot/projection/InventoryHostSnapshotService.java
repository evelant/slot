package dev.imagio.slot.projection;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.client.collection.CollectionStore;
import dev.imagio.slot.session.ChestLikeMenuLayout;
import dev.imagio.slot.session.InventoryHostDescriptor;
import dev.imagio.slot.session.InventorySourceDescriptor;
import dev.imagio.slot.session.InventorySourceRole;
import dev.imagio.slot.storage.adapter.ExternalStorageStackSnapshot;
import dev.imagio.slot.storage.provider.InventoryStackSnapshot;
import dev.imagio.slot.storage.provider.SupplementalCarriedSourceDescriptor;
import dev.imagio.slot.storage.provider.SupplementalCarriedSourceProviderRegistry;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class InventoryHostSnapshotService {
    private final InventoryWorkingSetBuilder workingSetBuilder = new InventoryWorkingSetBuilder();

    public InventoryViewData buildPlayerInventory(
            LocalPlayer player,
            CollectionStore collectionStore,
            InventoryHostDescriptor host
    ) {
        Map<String, InventoryViewData.SourceInfo> sources = host == null
                ? InventoryWorkingSetBuilder.sourceInfos(defaultPlayerSources())
                : InventoryWorkingSetBuilder.sourceInfos(host.sources());
        InventoryWorkingSetBuilder.Collector collector = workingSetBuilder.collector(sources);
        if (player != null) {
            if (host == null) {
                addDefaultPlayerSnapshots(player, collector);
            } else {
                addHostSourceSnapshots(player, host, collector, null);
            }
        }

        InventoryViewData data = collector.build(collectionStore, sourceId -> host != null && host.carriedSourceIds().contains(sourceId));
        SlotDebugLog.log(
                "Built player host working set for player={} snapshots={} aggregatedEntries={}",
                player == null ? "<null>" : player.getName().getString(),
                collector.snapshotCount(),
                data.entries().size()
        );
        return data;
    }

    public InventoryViewData buildContainerInventory(
            InventoryHostDescriptor host,
            LocalPlayer player,
            CollectionStore collectionStore,
            List<ExternalStorageStackSnapshot> primarySnapshots
    ) {
        InventoryWorkingSetBuilder.Collector collector = workingSetBuilder.collector(
                InventoryWorkingSetBuilder.sourceInfos(host.sources())
        );

        addHostSourceSnapshots(player, host, collector, primarySnapshots);

        InventoryViewData data = collector.build(collectionStore, host.carriedSourceIds()::contains);
        SlotDebugLog.log(
                "Built container host working set for menu={} aggregatedEntries={}",
                host.menu().getClass().getName(),
                data.entries().size()
        );
        return data;
    }

    private static void addHostSourceSnapshots(
            LocalPlayer player,
            InventoryHostDescriptor host,
            InventoryWorkingSetBuilder.Collector collector,
            List<ExternalStorageStackSnapshot> primarySnapshots
    ) {
        if (host == null) {
            return;
        }

        for (InventorySourceDescriptor source : host.sourceDescriptors()) {
            if (source.hidden() || source.toolOnly()) {
                continue;
            }

            if (host.topology().sourceMenuBacked(source.id())) {
                addMenuBackedSnapshots(host, source.id(), collector);
                continue;
            }

            if (source.domain() == dev.imagio.slot.session.InventorySourceDomain.HOST_STORAGE) {
                addProviderSnapshots(host, source, primarySnapshots, collector);
                continue;
            }

            if (source.domain() == dev.imagio.slot.session.InventorySourceDomain.SUPPLEMENTAL_CARRIED) {
                addSupplementalSnapshots(player, host, host.supplementalCarriedSources(source.id()), collector);
                continue;
            }

            if (source.domain() == dev.imagio.slot.session.InventorySourceDomain.PLAYER) {
                addPlayerSourceSnapshots(player, host, source, collector);
            }
        }
    }

    private static void addDefaultPlayerSnapshots(
            LocalPlayer player,
            InventoryWorkingSetBuilder.Collector collector
    ) {
        addMainSnapshots(player.getInventory().items, ChestLikeMenuLayout.SOURCE_PLAYER_MAIN, collector);
        addHotbarSnapshots(player.getInventory().items, ChestLikeMenuLayout.SOURCE_PLAYER_HOTBAR, collector);
        addArmorSnapshots(player.getInventory().armor, ChestLikeMenuLayout.SOURCE_PLAYER_ARMOR, collector);
        addOffhandSnapshots(player.getInventory().offhand, ChestLikeMenuLayout.SOURCE_PLAYER_OFFHAND, collector);
    }

    private static List<dev.imagio.slot.client.source.InventorySource> defaultPlayerSources() {
        return List.of(
                InventorySourceDescriptor.builder(ChestLikeMenuLayout.SOURCE_PLAYER_HOTBAR)
                        .label(net.minecraft.network.chat.Component.translatable("slot.source.hotbar"))
                        .role(InventorySourceRole.HOTBAR)
                        .slotCount(9)
                        .paneMembership(dev.imagio.slot.session.InventoryPaneMembership.CARRIED)
                        .build()
                        .toInventorySource(),
                InventorySourceDescriptor.builder(ChestLikeMenuLayout.SOURCE_PLAYER_MAIN)
                        .label(net.minecraft.network.chat.Component.translatable("slot.source.main"))
                        .role(InventorySourceRole.MAIN)
                        .slotCount(27)
                        .paneMembership(dev.imagio.slot.session.InventoryPaneMembership.CARRIED)
                        .build()
                        .toInventorySource(),
                InventorySourceDescriptor.builder(ChestLikeMenuLayout.SOURCE_PLAYER_ARMOR)
                        .label(net.minecraft.network.chat.Component.translatable("slot.source.armor"))
                        .role(InventorySourceRole.EQUIPMENT)
                        .groupId("armor")
                        .slotCount(4)
                        .paneMembership(dev.imagio.slot.session.InventoryPaneMembership.CARRIED)
                        .build()
                        .toInventorySource(),
                InventorySourceDescriptor.builder(ChestLikeMenuLayout.SOURCE_PLAYER_OFFHAND)
                        .label(net.minecraft.network.chat.Component.translatable("slot.source.offhand"))
                        .role(InventorySourceRole.OFFHAND)
                        .slotCount(1)
                        .paneMembership(dev.imagio.slot.session.InventoryPaneMembership.CARRIED)
                        .build()
                        .toInventorySource()
        );
    }

    private static void addProviderSnapshots(
            InventoryHostDescriptor host,
            InventorySourceDescriptor source,
            List<ExternalStorageStackSnapshot> primarySnapshots,
            InventoryWorkingSetBuilder.Collector collector
    ) {
        List<InventoryStackSnapshot> snapshots;
        InventorySourceDescriptor singleHostStorageSource = host.singleHostStorageSource();
        if (singleHostStorageSource != null
                && primarySnapshots != null
                && singleHostStorageSource.id().equals(source.id())) {
            snapshots = primarySnapshots.stream()
                    .map(snapshot -> new InventoryStackSnapshot(snapshot.handle(), snapshot.stack(), snapshot.count()))
                    .toList();
        } else {
            snapshots = host.providerSession().readSnapshots(host, source.id());
        }

        for (InventoryStackSnapshot snapshot : snapshots) {
            collector.addStack(snapshot.stack(), source.id(), snapshot.handle(), snapshot.count());
        }
    }

    private static void addMenuBackedSnapshots(
            InventoryHostDescriptor host,
            String sourceId,
            InventoryWorkingSetBuilder.Collector collector
    ) {
        if (host == null) {
            return;
        }
        for (int menuSlot : host.topology().menuSlotsForSource(sourceId)) {
            Slot slot = host.menu().getSlot(menuSlot);
            if (slot == null) {
                continue;
            }
            collector.addStack(slot.getItem(), sourceId, resolvedSourceSlotIndex(host, sourceId, menuSlot));
        }
    }

    private static void addPlayerSourceSnapshots(
            LocalPlayer player,
            InventoryHostDescriptor host,
            InventorySourceDescriptor source,
            InventoryWorkingSetBuilder.Collector collector
    ) {
        if (player == null || source == null) {
            return;
        }

        Set<Integer> menuBackedSourceSlots = menuBackedSourceSlots(host, source.id());
        switch (source.role()) {
            case MAIN -> {
                for (int inventorySlot = 9; inventorySlot < player.getInventory().items.size(); inventorySlot++) {
                    int sourceSlotIndex = ChestLikeMenuLayout.mainSourceSlotIndexForInventorySlot(inventorySlot);
                    if (sourceSlotIndex < 0 || menuBackedSourceSlots.contains(sourceSlotIndex)) {
                        continue;
                    }
                    collector.addStack(
                            player.getInventory().items.get(inventorySlot),
                            source.id(),
                            sourceSlotIndex
                    );
                }
            }
            case HOTBAR -> {
                for (int inventorySlot = 0; inventorySlot < Math.min(9, player.getInventory().items.size()); inventorySlot++) {
                    if (source.laneId() != null && !source.laneId().isBlank() && !"0".equals(source.laneId())) {
                        continue;
                    }
                    int sourceSlotIndex = ChestLikeMenuLayout.hotbarSourceSlotIndexForInventorySlot(inventorySlot);
                    if (sourceSlotIndex < 0 || menuBackedSourceSlots.contains(sourceSlotIndex)) {
                        continue;
                    }
                    collector.addStack(
                            player.getInventory().items.get(inventorySlot),
                            source.id(),
                            sourceSlotIndex
                    );
                }
            }
            case EQUIPMENT -> {
                for (int armorSlot = 0; armorSlot < player.getInventory().armor.size(); armorSlot++) {
                    int sourceSlotIndex = ChestLikeMenuLayout.armorSourceSlotIndexForInventorySlot(armorSlot);
                    if (sourceSlotIndex < 0 || menuBackedSourceSlots.contains(sourceSlotIndex)) {
                        continue;
                    }
                    collector.addStack(
                            player.getInventory().armor.get(armorSlot),
                            source.id(),
                            sourceSlotIndex
                    );
                }
            }
            case OFFHAND -> {
                if (!menuBackedSourceSlots.contains(0)) {
                    collector.addStack(player.getOffhandItem(), source.id(), 0);
                }
            }
            default -> {
            }
        }
    }

    private static void addSupplementalSnapshots(
            LocalPlayer player,
            InventoryHostDescriptor host,
            List<SupplementalCarriedSourceDescriptor> descriptors,
            InventoryWorkingSetBuilder.Collector collector
    ) {
        if (player == null || descriptors == null || descriptors.isEmpty()) {
            return;
        }

        Set<String> handledSources = new LinkedHashSet<>();
        for (SupplementalCarriedSourceDescriptor descriptor : descriptors) {
            if (!handledSources.add(descriptor.sourceId())) {
                continue;
            }
            for (var snapshot : SupplementalCarriedSourceProviderRegistry.readSnapshots(
                    player,
                    host,
                    descriptor.sourceId()
            )) {
                collector.addStack(snapshot.stack(), snapshot.sourceId(), snapshot.slotIndex());
            }
        }
    }

    private static void addHotbarSnapshots(
            NonNullList<ItemStack> inventory,
            String sourceId,
            InventoryWorkingSetBuilder.Collector collector
    ) {
        for (int slot = 0; slot < 9 && slot < inventory.size(); slot++) {
            collector.addStack(inventory.get(slot), sourceId, slot);
        }
    }

    private static void addMainSnapshots(
            NonNullList<ItemStack> inventory,
            String sourceId,
            InventoryWorkingSetBuilder.Collector collector
    ) {
        for (int slot = 9; slot < inventory.size(); slot++) {
            collector.addStack(inventory.get(slot), sourceId, slot - 9);
        }
    }

    private static void addArmorSnapshots(
            NonNullList<ItemStack> inventory,
            String sourceId,
            InventoryWorkingSetBuilder.Collector collector
    ) {
        for (int slot = 0; slot < inventory.size(); slot++) {
            collector.addStack(
                    inventory.get(slot),
                    sourceId,
                    ChestLikeMenuLayout.armorSourceSlotIndexForInventorySlot(slot)
            );
        }
    }

    private static void addOffhandSnapshots(
            NonNullList<ItemStack> inventory,
            String sourceId,
            InventoryWorkingSetBuilder.Collector collector
    ) {
        for (int slot = 0; slot < inventory.size(); slot++) {
            collector.addStack(inventory.get(slot), sourceId, slot);
        }
    }

    private static int resolvedSourceSlotIndex(InventoryHostDescriptor host, String sourceId, int menuSlot) {
        if (host == null) {
            return menuSlot;
        }
        List<Integer> menuSlots = host.topology().menuSlotsForSource(sourceId);
        int sourceSlotIndex = menuSlots.indexOf(menuSlot);
        return sourceSlotIndex >= 0 ? sourceSlotIndex : menuSlot;
    }

    private static Set<Integer> menuBackedSourceSlots(InventoryHostDescriptor host, String sourceId) {
        if (host == null || sourceId == null || !host.menuBacksSource(sourceId)) {
            return Set.of();
        }

        Set<Integer> sourceSlots = new LinkedHashSet<>();
        for (int menuSlot : host.topology().menuSlotsForSource(sourceId)) {
            int sourceSlotIndex = resolvedSourceSlotIndex(host, sourceId, menuSlot);
            if (sourceSlotIndex >= 0) {
                sourceSlots.add(sourceSlotIndex);
            }
        }
        return Set.copyOf(sourceSlots);
    }
}
