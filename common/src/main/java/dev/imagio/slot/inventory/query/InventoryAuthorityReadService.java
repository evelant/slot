package dev.imagio.slot.inventory.query;

import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.EquipmentGroupDescriptor;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventoryPaneMembership;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.InventorySourceRole;
import dev.imagio.slot.inventory.core.MenuCursorAccess;
import dev.imagio.slot.inventory.integration.PlayerInventoryExtension;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class InventoryAuthorityReadService {
    private InventoryAuthorityReadService() {
    }

    public static InventoryAuthoritySnapshot clientAuthority(Player player, InventoryHostDescriptor host) {
        return authority(host, source -> clientSourceSnapshot(player, host, source.id()));
    }

    public static InventoryAuthoritySnapshot serverAuthority(ServerPlayer player, InventoryHostDescriptor host) {
        return authority(host, source -> serverSourceSnapshot(player, host, source.id()));
    }

    public static InventorySourceSnapshot clientSourceSnapshot(
            Player player,
            InventoryHostDescriptor host,
            String sourceId
    ) {
        if (host == null || sourceId == null || sourceId.isBlank()) {
            return InventorySourceSnapshot.empty(sourceId == null || sourceId.isBlank() ? "__missing__" : sourceId);
        }
        InventorySourceDescriptor source = host.source(sourceId);
        if (source == null) {
            return InventorySourceSnapshot.empty(sourceId);
        }
        return switch (source.domain()) {
            case PLAYER -> readBuiltInSnapshot(player == null ? null : player.getInventory(), source);
            case PLAYER_EXTENSION -> {
                PlayerInventoryExtension extension = host.extensionOwningSource(sourceId);
                yield extension == null || player == null
                        ? InventorySourceSnapshot.empty(sourceId)
                        : safeSourceSnapshot(extension.readSourceSnapshot(player, host, sourceId), sourceId);
            }
            case HOST_STORAGE, TOOL_REGION -> safeSourceSnapshot(host.hostSession().readSourceSnapshot(host, sourceId), sourceId);
        };
    }

    public static InventorySourceSnapshot serverSourceSnapshot(
            ServerPlayer player,
            InventoryHostDescriptor host,
            String sourceId
    ) {
        if (host == null || sourceId == null || sourceId.isBlank()) {
            return InventorySourceSnapshot.empty(sourceId == null || sourceId.isBlank() ? "__missing__" : sourceId);
        }
        InventorySourceDescriptor source = host.source(sourceId);
        if (source == null) {
            return InventorySourceSnapshot.empty(sourceId);
        }
        return switch (source.domain()) {
            case PLAYER -> readBuiltInSnapshot(player == null ? null : player.getInventory(), source);
            case PLAYER_EXTENSION -> {
                PlayerInventoryExtension extension = host.extensionOwningSource(sourceId);
                yield extension == null || player == null
                        ? InventorySourceSnapshot.empty(sourceId)
                        : safeSourceSnapshot(extension.readSourceSnapshot(player, host, sourceId), sourceId);
            }
            case HOST_STORAGE, TOOL_REGION -> safeSourceSnapshot(host.hostSession().readSourceSnapshot(host, sourceId), sourceId);
        };
    }

    public static ItemStack currentStack(
            InventoryHostDescriptor host,
            ServerPlayer player,
            InventoryActionTarget target
    ) {
        if (host == null || target == null) {
            return ItemStack.EMPTY;
        }
        if (target instanceof InventoryActionTarget.CursorTarget) {
            return cursor(host.menu()).stack();
        }
        InventoryEntrySnapshot entry = entrySnapshot(serverAuthority(player, host), target);
        return entry == null || !entry.present() ? ItemStack.EMPTY : entry.stack().copy();
    }

    public static InventoryEntrySnapshot entrySnapshot(
            InventoryAuthoritySnapshot authority,
            InventoryActionTarget target
    ) {
        if (authority == null || target == null) {
            return null;
        }
        String sourceId = sourceId(authority.host(), target);
        if (sourceId.isBlank()) {
            return null;
        }
        InventorySourceSnapshot source = authority.sourceSnapshot(sourceId);
        if (source == null) {
            return null;
        }
        if (target instanceof InventoryActionTarget.SourceEntryTarget sourceEntryTarget) {
            return source.providerEntry(sourceEntryTarget.entryId());
        }
        if (target instanceof InventoryActionTarget.SourceTarget) {
            return null;
        }
        int slotIndex = slotIndex(authority.host(), target);
        return slotIndex < 0 ? null : source.slotEntry(slotIndex);
    }

    public static CursorStateSnapshot cursor(AbstractContainerMenu menu) {
        if (menu == null) {
            return CursorStateSnapshot.empty();
        }
        return new CursorStateSnapshot(MenuCursorAccess.get(menu), "");
    }

    public static List<InventorySourceDescriptor> sourcesInPane(
            InventoryHostDescriptor host,
            InventoryPaneMembership membership
    ) {
        return InventoryDomainQueryService.sourcesInPane(host, membership);
    }

    public static String sourceId(InventoryHostDescriptor host, InventoryActionTarget target) {
        if (host == null || target == null) {
            return "";
        }
        if (target instanceof InventoryActionTarget.SourceTarget sourceTarget) {
            return sourceTarget.sourceId();
        }
        if (target instanceof InventoryActionTarget.SourceSlotTarget sourceSlotTarget) {
            return sourceSlotTarget.sourceId();
        }
        if (target instanceof InventoryActionTarget.SourceEntryTarget sourceEntryTarget) {
            return sourceEntryTarget.sourceId();
        }
        if (target instanceof InventoryActionTarget.QuickAccessTarget quickAccessTarget) {
            dev.imagio.slot.inventory.core.QuickAccessLaneDescriptor lane = host.quickAccessLane(quickAccessTarget.laneId());
            return lane == null ? "" : lane.sourceId();
        }
        if (target instanceof InventoryActionTarget.EquipmentTarget equipmentTarget) {
            EquipmentGroupDescriptor group = host.equipmentGroup(equipmentTarget.groupId());
            return group == null ? "" : group.sourceId();
        }
        if (target instanceof InventoryActionTarget.ToolRegionTarget toolRegionTarget) {
            dev.imagio.slot.inventory.core.InventoryToolDescriptor tool = host.tool(toolRegionTarget.toolId());
            dev.imagio.slot.inventory.core.ToolRegionDescriptor region = tool == null ? null : tool.regions().stream()
                    .filter(candidate -> candidate != null && toolRegionTarget.regionId().equals(candidate.id()))
                    .findFirst()
                    .orElse(null);
            return region == null ? "" : region.linkedSourceId();
        }
        return "";
    }

    public static int slotIndex(InventoryHostDescriptor host, InventoryActionTarget target) {
        if (host == null || target == null) {
            return -1;
        }
        if (target instanceof InventoryActionTarget.SourceSlotTarget sourceSlotTarget) {
            return sourceSlotTarget.slotIndex();
        }
        if (target instanceof InventoryActionTarget.QuickAccessTarget quickAccessTarget) {
            return quickAccessTarget.slotIndex();
        }
        if (target instanceof InventoryActionTarget.EquipmentTarget equipmentTarget) {
            return equipmentTarget.slotIndex();
        }
        if (target instanceof InventoryActionTarget.ToolRegionTarget toolRegionTarget) {
            return toolRegionTarget.slotIndex();
        }
        return -1;
    }

    public static String entryId(InventoryActionTarget target) {
        return target instanceof InventoryActionTarget.SourceEntryTarget sourceEntryTarget
                ? sourceEntryTarget.entryId()
                : "";
    }

    private static InventoryAuthoritySnapshot authority(
            InventoryHostDescriptor host,
            java.util.function.Function<InventorySourceDescriptor, InventorySourceSnapshot> reader
    ) {
        if (host == null) {
            return InventoryAuthoritySnapshot.empty();
        }
        LinkedHashMap<String, InventorySourceSnapshot> sources = new LinkedHashMap<>();
        for (InventorySourceDescriptor source : host.sourceDescriptors()) {
            if (source == null) {
                continue;
            }
            sources.put(source.id(), safeSourceSnapshot(reader.apply(source), source.id()));
        }
        return new InventoryAuthoritySnapshot(host, Map.copyOf(sources), cursor(host.menu()));
    }

    private static InventorySourceSnapshot readBuiltInSnapshot(Inventory inventory, InventorySourceDescriptor source) {
        if (source == null) {
            return InventorySourceSnapshot.empty("__missing__");
        }
        if (inventory == null) {
            return new InventorySourceSnapshot(source.id(), builtInSlotCapacity(source), List.of(), "");
        }
        return switch (source.role()) {
            case MAIN -> new InventorySourceSnapshot(source.id(), 27, mainEntries(inventory), "");
            case QUICK_ACCESS -> new InventorySourceSnapshot(source.id(), builtInSlotCapacity(source), quickAccessEntries(inventory, source), "");
            case EQUIPMENT -> new InventorySourceSnapshot(source.id(), builtInSlotCapacity(source), equipmentEntries(inventory, source), "");
            case OFFHAND -> new InventorySourceSnapshot(source.id(), 1, offhandEntries(inventory, source.id()), "");
            default -> new InventorySourceSnapshot(source.id(), builtInSlotCapacity(source), List.of(), "");
        };
    }

    private static List<InventoryEntrySnapshot> mainEntries(Inventory inventory) {
        ArrayList<InventoryEntrySnapshot> entries = new ArrayList<>();
        int logicalIndex = 0;
        for (int inventorySlot = 9; inventorySlot < inventory.items.size(); inventorySlot++) {
            ItemStack stack = inventory.items.get(inventorySlot);
            if (stack != null && !stack.isEmpty()) {
                entries.add(new InventoryEntrySnapshot(
                        InventoryEntryKey.slot(BuiltinInventoryIds.PLAYER_MAIN, logicalIndex),
                        stack.copy(),
                        stack.getCount(),
                        ""
                ));
            }
            logicalIndex++;
        }
        return List.copyOf(entries);
    }

    private static List<InventoryEntrySnapshot> quickAccessEntries(Inventory inventory, InventorySourceDescriptor source) {
        if (!BuiltinInventoryIds.QUICK_ACCESS_LANE_0.equals(source.laneId())) {
            return List.of();
        }
        ArrayList<InventoryEntrySnapshot> entries = new ArrayList<>();
        for (int slot = 0; slot < Math.min(9, inventory.items.size()); slot++) {
            ItemStack stack = inventory.items.get(slot);
            if (stack != null && !stack.isEmpty()) {
                entries.add(new InventoryEntrySnapshot(
                        InventoryEntryKey.slot(source.id(), slot),
                        stack.copy(),
                        stack.getCount(),
                        ""
                ));
            }
        }
        return List.copyOf(entries);
    }

    private static List<InventoryEntrySnapshot> equipmentEntries(Inventory inventory, InventorySourceDescriptor source) {
        if (!BuiltinInventoryIds.EQUIPMENT_GROUP_ARMOR.equals(source.groupId())) {
            return List.of();
        }
        ArrayList<InventoryEntrySnapshot> entries = new ArrayList<>();
        for (int slot = 0; slot < inventory.armor.size(); slot++) {
            ItemStack stack = inventory.armor.get(slot);
            if (stack != null && !stack.isEmpty()) {
                entries.add(new InventoryEntrySnapshot(
                        InventoryEntryKey.slot(source.id(), slot),
                        stack.copy(),
                        stack.getCount(),
                        ""
                ));
            }
        }
        return List.copyOf(entries);
    }

    private static List<InventoryEntrySnapshot> offhandEntries(Inventory inventory, String sourceId) {
        if (inventory.offhand.isEmpty()) {
            return List.of();
        }
        ItemStack stack = inventory.offhand.get(0);
        if (stack == null || stack.isEmpty()) {
            return List.of();
        }
        return List.of(new InventoryEntrySnapshot(
                InventoryEntryKey.slot(sourceId, 0),
                stack.copy(),
                stack.getCount(),
                ""
        ));
    }

    private static int builtInSlotCapacity(InventorySourceDescriptor source) {
        if (source == null) {
            return 0;
        }
        if (source.role() == InventorySourceRole.MAIN) {
            return 27;
        }
        if (source.role() == InventorySourceRole.QUICK_ACCESS && BuiltinInventoryIds.QUICK_ACCESS_LANE_0.equals(source.laneId())) {
            return 9;
        }
        if (source.role() == InventorySourceRole.EQUIPMENT && BuiltinInventoryIds.EQUIPMENT_GROUP_ARMOR.equals(source.groupId())) {
            return 4;
        }
        if (source.role() == InventorySourceRole.OFFHAND) {
            return 1;
        }
        return Math.max(0, source.logicalSlotCount());
    }

    private static InventorySourceSnapshot safeSourceSnapshot(
            InventorySourceSnapshot snapshot,
            String sourceId
    ) {
        return snapshot == null ? InventorySourceSnapshot.empty(sourceId) : snapshot;
    }
}
