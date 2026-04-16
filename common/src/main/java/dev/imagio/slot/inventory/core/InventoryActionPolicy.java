package dev.imagio.slot.inventory.core;

import dev.imagio.slot.inventory.action.InventoryActionKind;
import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.workflow.domain.ProtectionPolicy;
import net.minecraft.world.item.ItemStack;

public final class InventoryActionPolicy {
    private InventoryActionPolicy() {
    }

    public static boolean allows(
            InventoryHostDescriptor host,
            InventoryActionKind kind,
            InventoryActionTarget target,
            ProtectionPolicy protectionPolicy
    ) {
        if (host == null || kind == null || target == null) {
            return false;
        }
        if (blockedByProtection(kind, target, null, ItemStack.EMPTY, protectionPolicy)) {
            return false;
        }

        return switch (target) {
            case InventoryActionTarget.CursorTarget ignored -> false;
            case InventoryActionTarget.SourceTarget sourceTarget -> {
                InventorySourceDescriptor source = host.source(sourceTarget.sourceId());
                yield source != null && source.actionable() && supports(source, kind);
            }
            case InventoryActionTarget.SourceSlotTarget slotTarget -> {
                InventorySourceDescriptor source = host.source(slotTarget.sourceId());
                yield source != null && source.actionable() && supports(source, kind);
            }
            case InventoryActionTarget.SourceEntryTarget sourceEntryTarget -> {
                InventorySourceDescriptor source = host.source(sourceEntryTarget.sourceId());
                yield source != null && source.actionable() && source.providerBacked() && supports(source, kind);
            }
            case InventoryActionTarget.QuickAccessTarget laneTarget -> {
                QuickAccessLaneDescriptor lane = host.quickAccessLane(laneTarget.laneId());
                yield lane != null && supports(lane, kind);
            }
            case InventoryActionTarget.EquipmentTarget equipmentTarget -> {
                EquipmentGroupDescriptor group = host.equipmentGroup(equipmentTarget.groupId());
                yield group != null && supports(group, kind);
            }
            case InventoryActionTarget.ToolRegionTarget regionTarget -> {
                InventoryToolDescriptor tool = host.tool(regionTarget.toolId());
                ToolRegionDescriptor region = tool == null ? null : tool.regions().stream()
                        .filter(candidate -> regionTarget.regionId().equals(candidate.id()))
                        .findFirst()
                        .orElse(null);
                if (region == null) {
                    yield false;
                }
                InventorySourceDescriptor linkedSource = region.linkedSourceId().isBlank() ? null : host.source(region.linkedSourceId());
                yield linkedSource != null
                        ? linkedSource.actionable() && supports(linkedSource, kind)
                        : supports(region, kind);
            }
            case InventoryActionTarget.ToolControlTarget controlTarget -> {
                InventoryToolDescriptor tool = host.tool(controlTarget.toolId());
                if (tool == null) {
                    yield false;
                }
                boolean matchesAction = tool.actions().stream()
                        .anyMatch(action -> action.stableId().equals(controlTarget.controlId()));
                boolean matchesToggle = tool.toggles().stream()
                        .anyMatch(toggle -> toggle.stableId().equals(controlTarget.controlId()));
                yield matchesAction || matchesToggle;
            }
        };
    }

    public static boolean blockedByProtection(
            InventoryActionKind kind,
            ItemIdentity identity,
            ItemStack stack,
            ProtectionPolicy protectionPolicy
    ) {
        return blockedByProtection(kind, null, identity, stack, protectionPolicy);
    }

    public static boolean blockedByProtection(
            InventoryActionKind kind,
            InventoryActionTarget target,
            ItemIdentity identity,
            ItemStack stack,
            ProtectionPolicy protectionPolicy
    ) {
        if (kind == null || protectionPolicy == null || !mutatesProtectedState(kind)) {
            return false;
        }
        if (target != null && protectionPolicy.protectsTarget(target, kind)) {
            return true;
        }
        if (identity != null && protectionPolicy.protects(identity, kind)) {
            return true;
        }
        return protectionPolicy.protectsPortableContainers() && PortableContainerClassifiers.isPortableContainer(stack);
    }

    private static boolean supports(InventorySourceDescriptor source, InventoryActionKind kind) {
        return switch (kind) {
            case TRANSFER, CURSOR_PICKUP, CURSOR_PLACE, CURSOR_SWAP, QUICK_MOVE, SWAP, DISTRIBUTE, COLLECT_MATCHING ->
                    source.supports(InventoryCapability.INSERT) || source.supports(InventoryCapability.EXTRACT);
            case ASSIGN -> source.supports(InventoryCapability.INSERT) && source.supports(InventoryCapability.EXTRACT);
            case USE -> source.supports(InventoryCapability.USE);
            case DROP_TO_WORLD -> source.supports(InventoryCapability.DROP);
            case TRASH, VOID, SORT_SOURCE, SET_FILTER, TOOL_ACTIVATE, TOOL_ACTION, TOOL_TOGGLE -> false;
        };
    }

    private static boolean supports(QuickAccessLaneDescriptor lane, InventoryActionKind kind) {
        return switch (kind) {
            case USE -> lane.supports(InventoryCapability.USE);
            case DROP_TO_WORLD -> lane.supports(InventoryCapability.DROP);
            case TRANSFER, CURSOR_PICKUP, CURSOR_PLACE, CURSOR_SWAP, QUICK_MOVE, SWAP, DISTRIBUTE, COLLECT_MATCHING ->
                    lane.supports(InventoryCapability.INSERT) || lane.supports(InventoryCapability.EXTRACT);
            case ASSIGN -> lane.supports(InventoryCapability.QUICK_ACCESS_ASSIGN);
            default -> false;
        };
    }

    private static boolean supports(EquipmentGroupDescriptor group, InventoryActionKind kind) {
        return switch (kind) {
            case ASSIGN -> group.supports(InventoryCapability.EQUIP) || group.supports(InventoryCapability.INSERT);
            case TRANSFER, CURSOR_PICKUP, CURSOR_PLACE, CURSOR_SWAP, QUICK_MOVE, SWAP ->
                    group.supports(InventoryCapability.UNEQUIP)
                            || group.supports(InventoryCapability.EXTRACT)
                            || group.supports(InventoryCapability.EQUIP)
                            || group.supports(InventoryCapability.INSERT);
            case USE -> group.supports(InventoryCapability.USE);
            case DROP_TO_WORLD -> group.supports(InventoryCapability.DROP);
            default -> false;
        };
    }

    private static boolean supports(ToolRegionDescriptor region, InventoryActionKind kind) {
        return switch (kind) {
            case TRANSFER, CURSOR_PICKUP, CURSOR_PLACE, CURSOR_SWAP, QUICK_MOVE, SWAP, DISTRIBUTE, COLLECT_MATCHING ->
                    region.supports(InventoryCapability.TOOL_REGION_MUTATION)
                            || region.supports(InventoryCapability.INSERT)
                            || region.supports(InventoryCapability.EXTRACT);
            case ASSIGN -> false;
            default -> false;
        };
    }

    private static boolean mutatesProtectedState(InventoryActionKind kind) {
        return switch (kind) {
            case TRANSFER, ASSIGN, SWAP, CURSOR_PICKUP, CURSOR_PLACE, CURSOR_SWAP, QUICK_MOVE,
                    DROP_TO_WORLD, TRASH, VOID, USE, SORT_SOURCE, DISTRIBUTE, COLLECT_MATCHING, SET_FILTER -> true;
            case TOOL_ACTIVATE, TOOL_ACTION, TOOL_TOGGLE -> false;
        };
    }

}
