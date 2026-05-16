package dev.imagio.slot.neoforge.client;

import dev.imagio.slot.neoforge.config.SlotClientConfig;
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

        return builder.build();
    }

    private static AbstractConfigListEntry<?> intMarginEntry(
            ConfigEntryBuilder entries,
            String labelKey,
            String tooltipKey,
            ModConfigSpec.IntValue value
    ) {
        return entries.startIntField(Component.translatable(labelKey), value.get())
                .setDefaultValue(DEFAULT_MARGIN)
                .setMin(MIN_MARGIN)
                .setMax(MAX_MARGIN)
                .setTooltip(Component.translatable(tooltipKey))
                .setSaveConsumer(next -> {
                    value.set(next);
                    value.save();
                })
                .build();
    }
}
