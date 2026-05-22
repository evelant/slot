package dev.imagio.slot.forge.client;

import com.mojang.blaze3d.font.GlyphProvider;
import dev.imagio.slot.platform.SlotResourceAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.tysontheember.emberstextapi.sdf.SDFProviderRegistry;

import java.util.List;

public final class ForgeSlotUiFonts {
    private static final ResourceLocation SLOT_UI_FONT = SlotResourceAccess.current().id("slot", "slot_ui");

    private ForgeSlotUiFonts() {
    }

    public static ResourceLocation uiFont() {
        // Embers skips SDF providers when FreeType is unavailable; use vanilla
        // text instead of routing SLOT labels to the missing-glyph font set.
        List<GlyphProvider> providers = SDFProviderRegistry.getProvidersForFont(SLOT_UI_FONT);
        return providers == null || providers.isEmpty() ? Minecraft.DEFAULT_FONT : SLOT_UI_FONT;
    }
}
