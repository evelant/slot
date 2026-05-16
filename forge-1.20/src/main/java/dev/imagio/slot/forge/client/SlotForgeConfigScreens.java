package dev.imagio.slot.forge.client;

import dev.imagio.slot.forge.config.SlotForgeClientConfig;
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

        return builder.build();
    }

    private static AbstractConfigListEntry<?> intMarginEntry(
            ConfigEntryBuilder entries,
            String labelKey,
            String tooltipKey,
            ForgeConfigSpec.IntValue value
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
