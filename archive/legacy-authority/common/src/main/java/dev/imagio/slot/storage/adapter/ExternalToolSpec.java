package dev.imagio.slot.storage.adapter;

import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public record ExternalToolSpec(
        String id,
        ExternalToolKind kind,
        Component title,
        ExternalToolPresentation preferredPresentation,
        int priority,
        int preferredHeight,
        List<ExternalToolSlotRegion> slotRegions,
        Set<ExternalToolCapability> capabilities,
        List<ExternalToolAction> actions,
        List<ExternalToolToggle> toggles
) {
    public ExternalToolSpec {
        id = id == null ? "" : id;
        kind = kind == null ? ExternalToolKind.CRAFTING_GRID : kind;
        preferredPresentation = preferredPresentation == null ? ExternalToolPresentation.DOCKED : preferredPresentation;
        preferredHeight = Math.max(0, preferredHeight);
        slotRegions = slotRegions == null ? List.of() : List.copyOf(slotRegions);
        capabilities = capabilities == null || capabilities.isEmpty()
                ? Set.of()
                : Set.copyOf(EnumSet.copyOf(capabilities));
        actions = actions == null ? List.of() : List.copyOf(actions);
        toggles = toggles == null ? List.of() : List.copyOf(toggles);
    }

    public Optional<ExternalToolSlotRegion> firstRegion(ExternalToolSlotRole role) {
        if (role == null) {
            return Optional.empty();
        }
        return slotRegions.stream().filter(region -> region.role() == role).findFirst();
    }

    public List<Integer> menuSlotsForRole(ExternalToolSlotRole role) {
        return firstRegion(role).map(ExternalToolSlotRegion::menuSlots).orElse(List.of());
    }

    public boolean supports(ExternalToolCapability capability) {
        return capability != null && capabilities.contains(capability);
    }

    public Optional<ExternalToolAction> action(ExternalToolActionId actionId) {
        if (actionId == null) {
            return Optional.empty();
        }
        return actions.stream().filter(action -> action.id() == actionId).findFirst();
    }

    public Optional<ExternalToolToggle> toggle(ExternalToolToggleId toggleId) {
        if (toggleId == null) {
            return Optional.empty();
        }
        return toggles.stream().filter(toggle -> toggle.id() == toggleId).findFirst();
    }

    public ExternalToolSpec withCapability(ExternalToolCapability capability) {
        if (capability == null || supports(capability)) {
            return this;
        }

        EnumSet<ExternalToolCapability> merged = capabilities.isEmpty()
                ? EnumSet.noneOf(ExternalToolCapability.class)
                : EnumSet.copyOf(capabilities);
        merged.add(capability);
        return new ExternalToolSpec(id, kind, title, preferredPresentation, priority, preferredHeight, slotRegions, merged, actions, toggles);
    }

    public ExternalToolSpec withToggle(ExternalToolToggle toggle) {
        if (toggle == null || this.toggle(toggle.id()).isPresent()) {
            return this;
        }

        List<ExternalToolToggle> merged = new ArrayList<>(toggles);
        merged.add(toggle);
        return new ExternalToolSpec(id, kind, title, preferredPresentation, priority, preferredHeight, slotRegions, capabilities, actions, merged);
    }

    public static ExternalToolSpec craftingGrid(
            String id,
            Component title,
            int preferredHeight,
            List<Integer> inputSlots,
            int resultSlot
    ) {
        return new ExternalToolSpec(
                id,
                ExternalToolKind.CRAFTING_GRID,
                title,
                ExternalToolPresentation.DOCKED,
                0,
                preferredHeight,
                List.of(
                        ExternalToolSlotRegion.grid("inputs", ExternalToolSlotRole.INPUT, 3, inputSlots),
                        ExternalToolSlotRegion.single("result", ExternalToolSlotRole.OUTPUT, resultSlot)
                ),
                EnumSet.of(
                        ExternalToolCapability.SLOT_INTERACTION,
                        ExternalToolCapability.SLOT_COUNTS,
                        ExternalToolCapability.RECIPE_TRANSFER,
                        ExternalToolCapability.DRAG_DISTRIBUTE,
                        ExternalToolCapability.CLEAR_GRID,
                        ExternalToolCapability.BALANCE_GRID,
                        ExternalToolCapability.ROTATE_GRID
                ),
                List.of(
                        ExternalToolAction.clearGrid(),
                        ExternalToolAction.balanceGrid(),
                        ExternalToolAction.rotateGrid()
                ),
                List.of()
        );
    }
}
