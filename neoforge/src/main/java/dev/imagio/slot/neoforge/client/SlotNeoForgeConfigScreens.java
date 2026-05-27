package dev.imagio.slot.neoforge.client;

import dev.imagio.slot.neoforge.config.SlotClientConfig;
import dev.imagio.slot.ui.workspace.RecentsStripUiBuilder;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class SlotNeoForgeConfigScreens {
    private static final int DEFAULT_MARGIN = 0;
    private static final int MIN_MARGIN = 0;
    private static final int MAX_MARGIN = 400;
    private static final int MIN_CENTER_OFFSET = -400;
    private static final int MAX_CENTER_OFFSET = 400;

    private SlotNeoForgeConfigScreens() {
    }

    public static void register(ModContainer container) {
        container.registerExtensionPoint(
                IConfigScreenFactory.class,
                (IConfigScreenFactory) SlotNeoForgeConfigScreens::create
        );
    }

    private static Screen create(ModContainer ignored, Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("slot.config.title"));
        ConfigEntryBuilder entries = builder.entryBuilder();
        ConfigCategory category = builder.getOrCreateCategory(Component.translatable("slot.config.category.interface"));

        category.addEntry(entries.startBooleanToggle(
                        Component.translatable("slot.config.contextual_suggestion_debug_tooltips"),
                        SlotClientConfig.CLIENT.contextualSuggestionDebugTooltips.get())
                .setDefaultValue(false)
                .setTooltip(Component.translatable("slot.config.contextual_suggestion_debug_tooltips.tooltip"))
                .setSaveConsumer(next -> {
                    SlotClientConfig.CLIENT.contextualSuggestionDebugTooltips.set(next);
                    SlotClientConfig.CLIENT.contextualSuggestionDebugTooltips.save();
                })
                .build());
        category.addEntry(intMarginEntry(
                entries,
                "slot.config.sidebar_left_margin",
                "slot.config.sidebar_left_margin.tooltip",
                SlotClientConfig.CLIENT.sidebarLeftMargin
        ));
        category.addEntry(intMarginEntry(
                entries,
                "slot.config.sidebar_top_margin",
                "slot.config.sidebar_top_margin.tooltip",
                SlotClientConfig.CLIENT.sidebarTopMargin
        ));
        category.addEntry(intMarginEntry(
                entries,
                "slot.config.sidebar_bottom_margin",
                "slot.config.sidebar_bottom_margin.tooltip",
                SlotClientConfig.CLIENT.sidebarBottomMargin
        ));
        category.addEntry(intEntry(
                entries,
                "slot.config.recents_horizontal_offset",
                "slot.config.recents_horizontal_offset.tooltip",
                SlotClientConfig.CLIENT.recentsHorizontalOffset,
                RecentsStripUiBuilder.DEFAULT_HORIZONTAL_OFFSET_PX,
                MIN_CENTER_OFFSET,
                MAX_CENTER_OFFSET
        ));
        category.addEntry(intEntry(
                entries,
                "slot.config.recents_top_offset",
                "slot.config.recents_top_offset.tooltip",
                SlotClientConfig.CLIENT.recentsTopOffset,
                RecentsStripUiBuilder.DEFAULT_TOP_OFFSET_PX,
                MIN_MARGIN,
                MAX_MARGIN
        ));
        category.addEntry(intMarginEntry(
                entries,
                "slot.config.task_panel_right_margin",
                "slot.config.task_panel_right_margin.tooltip",
                SlotClientConfig.CLIENT.taskPanelRightMargin
        ));
        category.addEntry(intMarginEntry(
                entries,
                "slot.config.task_panel_top_margin",
                "slot.config.task_panel_top_margin.tooltip",
                SlotClientConfig.CLIENT.taskPanelTopMargin
        ));
        category.addEntry(intMarginEntry(
                entries,
                "slot.config.task_panel_bottom_margin",
                "slot.config.task_panel_bottom_margin.tooltip",
                SlotClientConfig.CLIENT.taskPanelBottomMargin
        ));

        return builder.build();
    }

    private static AbstractConfigListEntry<?> intMarginEntry(
            ConfigEntryBuilder entries,
            String labelKey,
            String tooltipKey,
            ModConfigSpec.IntValue value
    ) {
        return intEntry(entries, labelKey, tooltipKey, value, DEFAULT_MARGIN, MIN_MARGIN, MAX_MARGIN);
    }

    private static AbstractConfigListEntry<?> intEntry(
            ConfigEntryBuilder entries,
            String labelKey,
            String tooltipKey,
            ModConfigSpec.IntValue value,
            int defaultValue,
            int min,
            int max
    ) {
        return entries.startIntField(Component.translatable(labelKey), value.get())
                .setDefaultValue(defaultValue)
                .setMin(min)
                .setMax(max)
                .setTooltip(Component.translatable(tooltipKey))
                .setSaveConsumer(next -> {
                    value.set(next);
                    value.save();
                })
                .build();
    }
}
