package dev.imagio.slot.forge.client;

import dev.imagio.slot.forge.config.SlotForgeClientConfig;
import dev.imagio.slot.ui.workspace.RecentsStripUiBuilder;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public final class SlotForgeConfigScreens {
    private static final int DEFAULT_MARGIN = 0;
    private static final int MIN_MARGIN = 0;
    private static final int MAX_MARGIN = 400;
    private static final int MIN_CENTER_OFFSET = -400;
    private static final int MAX_CENTER_OFFSET = 400;

    private SlotForgeConfigScreens() {
    }

    public static void register(FMLJavaModLoadingContext context) {
        context.registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(SlotForgeConfigScreens::create)
        );
    }

    private static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("slot.config.title"));
        ConfigEntryBuilder entries = builder.entryBuilder();
        ConfigCategory category = builder.getOrCreateCategory(Component.translatable("slot.config.category.interface"));

        category.addEntry(entries.startBooleanToggle(
                        Component.translatable("slot.config.contextual_suggestion_debug_tooltips"),
                        SlotForgeClientConfig.CLIENT.contextualSuggestionDebugTooltips.get())
                .setDefaultValue(false)
                .setTooltip(Component.translatable("slot.config.contextual_suggestion_debug_tooltips.tooltip"))
                .setSaveConsumer(next -> {
                    SlotForgeClientConfig.CLIENT.contextualSuggestionDebugTooltips.set(next);
                    SlotForgeClientConfig.CLIENT.contextualSuggestionDebugTooltips.save();
                })
                .build());
        category.addEntry(intMarginEntry(
                entries,
                "slot.config.sidebar_left_margin",
                "slot.config.sidebar_left_margin.tooltip",
                SlotForgeClientConfig.CLIENT.sidebarLeftMargin
        ));
        category.addEntry(intMarginEntry(
                entries,
                "slot.config.sidebar_top_margin",
                "slot.config.sidebar_top_margin.tooltip",
                SlotForgeClientConfig.CLIENT.sidebarTopMargin
        ));
        category.addEntry(intMarginEntry(
                entries,
                "slot.config.sidebar_bottom_margin",
                "slot.config.sidebar_bottom_margin.tooltip",
                SlotForgeClientConfig.CLIENT.sidebarBottomMargin
        ));
        category.addEntry(intEntry(
                entries,
                "slot.config.recents_horizontal_offset",
                "slot.config.recents_horizontal_offset.tooltip",
                SlotForgeClientConfig.CLIENT.recentsHorizontalOffset,
                RecentsStripUiBuilder.DEFAULT_HORIZONTAL_OFFSET_PX,
                MIN_CENTER_OFFSET,
                MAX_CENTER_OFFSET
        ));
        category.addEntry(intEntry(
                entries,
                "slot.config.recents_top_offset",
                "slot.config.recents_top_offset.tooltip",
                SlotForgeClientConfig.CLIENT.recentsTopOffset,
                RecentsStripUiBuilder.DEFAULT_TOP_OFFSET_PX,
                MIN_MARGIN,
                MAX_MARGIN
        ));
        category.addEntry(intMarginEntry(
                entries,
                "slot.config.craft_run_right_margin",
                "slot.config.craft_run_right_margin.tooltip",
                SlotForgeClientConfig.CLIENT.craftRunRightMargin
        ));
        category.addEntry(intMarginEntry(
                entries,
                "slot.config.craft_run_top_margin",
                "slot.config.craft_run_top_margin.tooltip",
                SlotForgeClientConfig.CLIENT.craftRunTopMargin
        ));
        category.addEntry(intMarginEntry(
                entries,
                "slot.config.craft_run_bottom_margin",
                "slot.config.craft_run_bottom_margin.tooltip",
                SlotForgeClientConfig.CLIENT.craftRunBottomMargin
        ));

        return builder.build();
    }

    private static AbstractConfigListEntry<?> intMarginEntry(
            ConfigEntryBuilder entries,
            String labelKey,
            String tooltipKey,
            ForgeConfigSpec.IntValue value
    ) {
        return intEntry(entries, labelKey, tooltipKey, value, DEFAULT_MARGIN, MIN_MARGIN, MAX_MARGIN);
    }

    private static AbstractConfigListEntry<?> intEntry(
            ConfigEntryBuilder entries,
            String labelKey,
            String tooltipKey,
            ForgeConfigSpec.IntValue value,
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
