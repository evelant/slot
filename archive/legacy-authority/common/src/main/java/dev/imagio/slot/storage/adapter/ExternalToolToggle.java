package dev.imagio.slot.storage.adapter;

import net.minecraft.network.chat.Component;

public record ExternalToolToggle(
        ExternalToolToggleId id,
        Component enabledLabel,
        Component disabledLabel,
        Component enabledTooltip,
        Component disabledTooltip
) {
    public ExternalToolToggle {
        id = id == null ? ExternalToolToggleId.AUTO_REFILL : id;
        enabledLabel = enabledLabel == null ? Component.empty() : enabledLabel;
        disabledLabel = disabledLabel == null ? Component.empty() : disabledLabel;
        enabledTooltip = enabledTooltip == null ? Component.empty() : enabledTooltip;
        disabledTooltip = disabledTooltip == null ? Component.empty() : disabledTooltip;
    }

    public Component label(boolean enabled) {
        return enabled ? enabledLabel : disabledLabel;
    }

    public Component tooltip(boolean enabled) {
        return enabled ? enabledTooltip : disabledTooltip;
    }

    public static ExternalToolToggle autoRefill() {
        return new ExternalToolToggle(
                ExternalToolToggleId.AUTO_REFILL,
                Component.translatable("slot.screen.container.tool_panel.crafting.refill.on"),
                Component.translatable("slot.screen.container.tool_panel.crafting.refill.off"),
                Component.translatable("slot.screen.container.tool_panel.crafting.refill.on.tooltip"),
                Component.translatable("slot.screen.container.tool_panel.crafting.refill.off.tooltip")
        );
    }
}
