package dev.imagio.slot.neoforge.mixin;

import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Exposes {@code Screen.addRenderableWidget} so SLOT can mount its
 * sidebar {@code ModularUIWidget} onto vanilla container screens
 * without subclassing them. See
 * {@code dev.imagio.slot.neoforge.client.screen.SlotContainerSidebar}.
 */
@Mixin(Screen.class)
public interface ScreenInvoker {
    @Invoker("addRenderableWidget")
    <T extends GuiEventListener & Renderable & NarratableEntry> T slot$addRenderableWidget(T widget);
}
