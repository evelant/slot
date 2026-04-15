package dev.imagio.slot.inventory.core;

import dev.imagio.slot.inventory.action.InventoryActionTarget;

public final class InventoryCraftingSurfaceSupport {
    private InventoryCraftingSurfaceSupport() {
    }

    public static ResolvedCraftingSurface resolve(
            InventoryHostDescriptor host,
            String toolId
    ) {
        if (host == null || toolId == null || toolId.isBlank()) {
            return ResolvedCraftingSurface.empty();
        }
        InventoryToolDescriptor tool = host.tool(toolId);
        CraftingSurfaceDescriptor surface = tool == null ? null : tool.craftingSurface();
        if (tool == null || surface == null || !surface.present()) {
            return ResolvedCraftingSurface.empty();
        }
        ToolRegionDescriptor inputRegion = tool.regions().stream()
                .filter(region -> region != null && region.role() == ToolRegionRole.INPUT)
                .findFirst()
                .orElse(null);
        ToolRegionDescriptor outputRegion = tool.regions().stream()
                .filter(region -> region != null && region.role() == ToolRegionRole.OUTPUT)
                .findFirst()
                .orElse(null);
        return new ResolvedCraftingSurface(tool, surface, inputRegion, outputRegion);
    }

    public static ResolvedCraftingSurface resolve(
            InventoryHostDescriptor host,
            InventoryActionTarget target
    ) {
        if (target == null) {
            return ResolvedCraftingSurface.empty();
        }
        return switch (target) {
            case InventoryActionTarget.ToolRegionTarget toolRegionTarget -> resolve(host, toolRegionTarget.toolId());
            case InventoryActionTarget.ToolControlTarget toolControlTarget -> resolve(host, toolControlTarget.toolId());
            default -> resolveByLinkedSlot(host, target);
        };
    }

    public static boolean isCraftingInputTarget(
            InventoryHostDescriptor host,
            InventoryActionTarget target
    ) {
        return resolve(host, target).matchesInputTarget(target);
    }

    public static boolean isCraftingOutputTarget(
            InventoryHostDescriptor host,
            InventoryActionTarget target
    ) {
        return resolve(host, target).matchesOutputTarget(target);
    }

    public static boolean touchesCraftingSurface(
            InventoryHostDescriptor host,
            InventoryActionTarget target
    ) {
        ResolvedCraftingSurface surface = resolve(host, target);
        return surface.present() && (surface.matchesInputTarget(target) || surface.matchesOutputTarget(target) || surface.matchesControlTarget(target));
    }

    private static ResolvedCraftingSurface resolveByLinkedSlot(
            InventoryHostDescriptor host,
            InventoryActionTarget target
    ) {
        if (!(target instanceof InventoryActionTarget.SourceSlotTarget sourceSlotTarget) || host == null) {
            return ResolvedCraftingSurface.empty();
        }
        for (InventoryToolDescriptor tool : host.toolDescriptors()) {
            ResolvedCraftingSurface resolved = resolve(host, tool.id());
            if (resolved.present() && (resolved.matchesInputTarget(sourceSlotTarget) || resolved.matchesOutputTarget(sourceSlotTarget))) {
                return resolved;
            }
        }
        return ResolvedCraftingSurface.empty();
    }

    public record ResolvedCraftingSurface(
            InventoryToolDescriptor tool,
            CraftingSurfaceDescriptor surface,
            ToolRegionDescriptor inputRegion,
            ToolRegionDescriptor outputRegion
    ) {
        public static ResolvedCraftingSurface empty() {
            return new ResolvedCraftingSurface(null, null, null, null);
        }

        public boolean present() {
            return tool != null && surface != null && surface.present();
        }

        public int inputCount() {
            return surface == null ? 0 : surface.inputSlotCount();
        }

        public boolean validInputIndex(int inputIndex) {
            return inputIndex >= 0 && inputIndex < inputCount();
        }

        public InventoryActionTarget inputTarget(int inputIndex) {
            if (!validInputIndex(inputIndex) || surface == null) {
                return null;
            }
            if (inputRegion != null) {
                return new InventoryActionTarget.ToolRegionTarget(tool.id(), inputRegion.id(), inputIndex);
            }
            return surface.inputSlotTarget(inputIndex);
        }

        public InventoryActionTarget outputTarget() {
            if (!present()) {
                return null;
            }
            if (outputRegion != null) {
                return new InventoryActionTarget.ToolRegionTarget(tool.id(), outputRegion.id(), 0);
            }
            return surface.outputSlotTarget();
        }

        public boolean supportsAction(InventoryToolActionId actionId) {
            return present()
                    && actionId != null
                    && tool.actions().stream().anyMatch(action -> action.id() == actionId);
        }

        public boolean supportsToggle(InventoryToolToggleId toggleId) {
            return present()
                    && toggleId != null
                    && tool.toggles().stream().anyMatch(toggle -> toggle.id() == toggleId);
        }

        public String actionStableId(InventoryToolActionId actionId) {
            if (!present() || actionId == null) {
                return "";
            }
            return tool.actions().stream()
                    .filter(action -> action.id() == actionId)
                    .map(InventoryToolAction::stableId)
                    .findFirst()
                    .orElse("");
        }

        public String toggleStableId(InventoryToolToggleId toggleId) {
            if (!present() || toggleId == null) {
                return "";
            }
            return tool.toggles().stream()
                    .filter(toggle -> toggle.id() == toggleId)
                    .map(InventoryToolToggle::stableId)
                    .findFirst()
                    .orElse("");
        }

        public boolean matchesInputTarget(InventoryActionTarget target) {
            if (!present() || target == null) {
                return false;
            }
            if (target instanceof InventoryActionTarget.ToolRegionTarget toolRegionTarget) {
                return inputRegion != null
                        && tool.id().equals(toolRegionTarget.toolId())
                        && inputRegion.id().equals(toolRegionTarget.regionId())
                        && validInputIndex(toolRegionTarget.slotIndex());
            }
            if (!(target instanceof InventoryActionTarget.SourceSlotTarget sourceSlotTarget)) {
                return false;
            }
            for (int index = 0; index < inputCount(); index++) {
                InventoryActionTarget.SourceSlotTarget inputTarget = surface.inputSlotTarget(index);
                if (inputTarget != null
                        && inputTarget.sourceId().equals(sourceSlotTarget.sourceId())
                        && inputTarget.slotIndex() == sourceSlotTarget.slotIndex()) {
                    return true;
                }
            }
            return false;
        }

        public boolean matchesOutputTarget(InventoryActionTarget target) {
            if (!present() || target == null) {
                return false;
            }
            if (target instanceof InventoryActionTarget.ToolRegionTarget toolRegionTarget) {
                return outputRegion != null
                        && tool.id().equals(toolRegionTarget.toolId())
                        && outputRegion.id().equals(toolRegionTarget.regionId())
                        && toolRegionTarget.slotIndex() == 0;
            }
            if (!(target instanceof InventoryActionTarget.SourceSlotTarget sourceSlotTarget) || surface.outputSlotTarget() == null) {
                return false;
            }
            return surface.outputSlotTarget().sourceId().equals(sourceSlotTarget.sourceId())
                    && surface.outputSlotTarget().slotIndex() == sourceSlotTarget.slotIndex();
        }

        public boolean matchesControlTarget(InventoryActionTarget target) {
            return target instanceof InventoryActionTarget.ToolControlTarget toolControlTarget
                    && present()
                    && tool.id().equals(toolControlTarget.toolId());
        }
    }
}
