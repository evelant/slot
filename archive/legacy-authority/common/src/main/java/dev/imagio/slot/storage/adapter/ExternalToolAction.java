package dev.imagio.slot.storage.adapter;

import net.minecraft.network.chat.Component;

public record ExternalToolAction(
        ExternalToolActionId id,
        Component label,
        Component tooltip
) {
    public ExternalToolAction {
        id = id == null ? ExternalToolActionId.CLEAR_GRID : id;
        label = label == null ? Component.empty() : label;
        tooltip = tooltip == null ? Component.empty() : tooltip;
    }

    public static ExternalToolAction clearGrid() {
        return new ExternalToolAction(
                ExternalToolActionId.CLEAR_GRID,
                Component.translatable("slot.screen.container.tool_panel.crafting.clear"),
                Component.translatable("slot.screen.container.tool_panel.crafting.clear.tooltip")
        );
    }

    public static ExternalToolAction balanceGrid() {
        return new ExternalToolAction(
                ExternalToolActionId.BALANCE_GRID,
                Component.translatable("slot.screen.container.tool_panel.crafting.balance"),
                Component.translatable("slot.screen.container.tool_panel.crafting.balance.tooltip")
        );
    }

    public static ExternalToolAction rotateGrid() {
        return new ExternalToolAction(
                ExternalToolActionId.ROTATE_GRID,
                Component.translatable("slot.screen.container.tool_panel.crafting.rotate"),
                Component.translatable("slot.screen.container.tool_panel.crafting.rotate.tooltip")
        );
    }
}
