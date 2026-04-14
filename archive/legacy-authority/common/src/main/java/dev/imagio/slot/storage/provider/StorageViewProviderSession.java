package dev.imagio.slot.storage.provider;

import dev.imagio.slot.capability.ToolCapabilityDescriptor;
import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.session.ChestLikeMenuLayout;
import dev.imagio.slot.session.HostTopologyDescriptor;
import dev.imagio.slot.session.InventoryHostDescriptor;
import dev.imagio.slot.session.InventoryPaneMembership;
import dev.imagio.slot.session.InventorySourceDescriptor;
import dev.imagio.slot.session.InventoryToolDescriptor;
import dev.imagio.slot.storage.adapter.ExternalStorageStackSnapshot;
import dev.imagio.slot.storage.adapter.ExternalToolToggleId;
import dev.imagio.slot.storage.adapter.StorageTransferMode;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface StorageViewProviderSession {
    String providerId();

    default List<InventorySourceDescriptor> hostSources() {
        InventorySourceDescriptor primary = normalizePrimarySource(primaryStorageSource());
        return primary == null ? List.of() : List.of(primary);
    }

    default HostTopologyDescriptor topology() {
        InventorySourceDescriptor primary = normalizePrimarySource(primaryStorageSource());
        if (primary == null) {
            return HostTopologyDescriptor.empty();
        }
        List<Integer> primaryMenuSlots = primaryMenuSlots();
        java.util.LinkedHashMap<Integer, String> sourceIdByMenuSlot = new java.util.LinkedHashMap<>();
        for (int menuSlot : primaryMenuSlots) {
            sourceIdByMenuSlot.put(menuSlot, primary.id());
        }
        java.util.LinkedHashMap<String, List<Integer>> toolRegions = new java.util.LinkedHashMap<>();
        for (InventoryToolDescriptor tool : tools()) {
            for (dev.imagio.slot.session.ToolRegionDescriptor region : tool.regions()) {
                toolRegions.put(region.id(), region.logicalSlots());
            }
        }
        return new HostTopologyDescriptor(
                java.util.Map.of(primary.id(), primaryMenuSlots),
                sourceIdByMenuSlot,
                toolRegions
        );
    }

    default List<ToolCapabilityDescriptor> toolDescriptors() {
        return List.of();
    }

    default List<InventoryToolDescriptor> tools() {
        return toolDescriptors().stream()
                .map(tool -> InventoryToolDescriptor.fromLegacy(
                        tool.providerId(),
                        tool.toolSpec(),
                        tool.live(),
                        tool.activationCommand(),
                        tool.toggleStates(),
                        tool.statePayload()
                ))
                .toList();
    }

    default List<InventoryStackSnapshot> readSnapshots(InventoryHostDescriptor host, String sourceId) {
        AbstractContainerMenu menu = host == null ? null : host.menu();
        InventorySourceDescriptor primary = normalizePrimarySource(primaryStorageSource());
        if (menu == null || primary == null || !primary.id().equals(sourceId)) {
            return List.of();
        }
        return readClientPrimarySnapshots(menu).stream()
                .map(snapshot -> new InventoryStackSnapshot(snapshot.handle(), snapshot.stack(), snapshot.count()))
                .toList();
    }

    default MutationResult applyMutation(InventoryHostDescriptor host, InventoryMutation mutation) {
        AbstractContainerMenu menu = host == null ? null : host.menu();
        ServerPlayer player = mutation == null ? null : mutation.player();
        InventorySourceDescriptor primary = normalizePrimarySource(primaryStorageSource());
        if (mutation == null || menu == null || primary == null || !primary.id().equals(mutation.sourceId())) {
            return MutationResult.blocked("unsupported_source", mutation == null ? ItemStack.EMPTY : mutation.stack());
        }
        return switch (mutation.kind()) {
            case EXTRACT -> MutationResult.success(extractFromPrimary(menu, player, mutation.identity(), mutation.transferMode()));
            case INSERT -> MutationResult.success(insertIntoPrimary(menu, player, mutation.stack()));
            case ACTIVATE_TARGET, UNSPECIFIED -> MutationResult.blocked("unsupported_mutation", mutation.stack());
        };
    }

    default ToolActionResult activateTool(InventoryHostDescriptor host, String toolId) {
        return activateTool(host == null ? null : host.menu(), toolId)
                ? ToolActionResult.success()
                : ToolActionResult.blocked("unsupported_tool_activation");
    }

    default boolean activateTool(AbstractContainerMenu menu, String toolId) {
        return false;
    }

    default ToolActionResult setToolToggle(
            InventoryHostDescriptor host,
            String toolId,
            ExternalToolToggleId toggleId,
            boolean enabled
    ) {
        return setToolToggle(host == null ? null : host.menu(), toolId, toggleId, enabled)
                ? ToolActionResult.success()
                : ToolActionResult.blocked("unsupported_tool_toggle");
    }

    default boolean setToolToggle(
            AbstractContainerMenu menu,
            String toolId,
            ExternalToolToggleId toggleId,
            boolean enabled
    ) {
        return false;
    }

    default String diagnostics() {
        return "";
    }

    @Deprecated(forRemoval = false)
    default InventorySourceDescriptor primaryStorageSource() {
        return hostSources().stream()
                .filter(source -> source.role() == dev.imagio.slot.session.InventorySourceRole.PRIMARY_STORAGE)
                .findFirst()
                .map(StorageViewProviderSession::normalizePrimarySource)
                .orElse(null);
    }

    @Deprecated(forRemoval = false)
    default List<Integer> primaryMenuSlots() {
        InventorySourceDescriptor primary = primaryStorageSource();
        return primary == null ? List.of() : topology().menuSlotsForSource(primary.id());
    }

    @Deprecated(forRemoval = false)
    default List<ExternalStorageStackSnapshot> readClientPrimarySnapshots(AbstractContainerMenu menu) {
        InventorySourceDescriptor primary = primaryStorageSource();
        if (menu == null || primary == null) {
            return List.of();
        }
        return readSnapshots(InventoryHostDescriptor.compatibilityHost(menu, this), primary.id()).stream()
                .map(snapshot -> new ExternalStorageStackSnapshot(snapshot.handle(), snapshot.stack(), snapshot.count()))
                .toList();
    }

    @Deprecated(forRemoval = false)
    default ItemStack extractFromPrimary(AbstractContainerMenu menu, ServerPlayer player, ItemIdentity identity, StorageTransferMode mode) {
        InventorySourceDescriptor primary = primaryStorageSource();
        if (menu == null || primary == null) {
            return ItemStack.EMPTY;
        }
        MutationResult result = applyMutation(
                InventoryHostDescriptor.compatibilityHost(menu, this),
                InventoryMutation.extract(
                        InventoryHostDescriptor.compatibilityHost(menu, this),
                        player,
                        primary.id(),
                        identity,
                        mode
                )
        );
        return result.stackRemainder();
    }

    @Deprecated(forRemoval = false)
    default ItemStack insertIntoPrimary(AbstractContainerMenu menu, ServerPlayer player, ItemStack stack) {
        InventorySourceDescriptor primary = primaryStorageSource();
        if (menu == null || primary == null) {
            return stack == null ? ItemStack.EMPTY : stack;
        }
        MutationResult result = applyMutation(
                InventoryHostDescriptor.compatibilityHost(menu, this),
                InventoryMutation.insert(
                        InventoryHostDescriptor.compatibilityHost(menu, this),
                        player,
                        primary.id(),
                        stack
                )
        );
        return result.stackRemainder();
    }

    default List<InventorySourceDescriptor> sourceDescriptors() {
        return hostSources();
    }

    default boolean primaryStorageIsCarried() {
        InventorySourceDescriptor primary = normalizePrimarySource(primaryStorageSource());
        return primary != null && primary.inCarriedInventory();
    }

    default String primaryStorageLabel() {
        InventorySourceDescriptor primary = normalizePrimarySource(primaryStorageSource());
        return primary == null ? "" : primary.label().getString();
    }

    default int primaryStorageSlotCount() {
        InventorySourceDescriptor primary = normalizePrimarySource(primaryStorageSource());
        return primary == null ? 0 : primary.slotCount();
    }

    default boolean primaryStorageMenuBacked() {
        InventorySourceDescriptor primary = normalizePrimarySource(primaryStorageSource());
        return primary != null && primary.menuBacked();
    }

    private static InventorySourceDescriptor normalizePrimarySource(InventorySourceDescriptor source) {
        if (source == null) {
            return null;
        }
        InventoryPaneMembership paneMembership = source.paneMembership();
        if (source.role() == dev.imagio.slot.session.InventorySourceRole.PRIMARY_STORAGE
                && paneMembership == InventoryPaneMembership.EXTERNAL
                && ChestLikeMenuLayout.SOURCE_CARRIED_STORAGE.equals(source.id())) {
            paneMembership = InventoryPaneMembership.CARRIED;
        }
        return new InventorySourceDescriptor(
                source.id(),
                source.label(),
                source.domain(),
                source.role(),
                source.laneId(),
                source.groupId(),
                source.logicalSlotCount(),
                source.backingKind(),
                source.capabilities(),
                source.actionRoute(),
                paneMembership,
                source.diagnostics(),
                source.stableOrder()
        );
    }

}
