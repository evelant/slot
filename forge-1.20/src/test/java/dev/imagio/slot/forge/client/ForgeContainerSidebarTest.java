package dev.imagio.slot.forge.client;

import dev.imagio.slot.forge.ui.ForgeWorkspaceSurface;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ForgeContainerSidebarTest {
    @Test
    void focusedVanillaEditBoxCountsAsHostTextInput() {
        EditBox editBox = new EditBox(null, 0, 0, 80, 12, Component.literal("Name"));
        editBox.setFocused(true);

        assertTrue(ForgeContainerSidebar.isTextInputFocused(editBox));
    }

    @Test
    void focusedGenericHostWidgetDoesNotBlockSlotHotkeys() {
        FocusableWidget widget = new FocusableWidget(true);

        assertFalse(ForgeContainerSidebar.isTextInputFocused(widget));
    }

    @Test
    void nestedFocusedGenericHostWidgetDoesNotBlockSlotHotkeys() {
        FocusableWidget widget = new FocusableWidget(true);
        FocusContainer container = new FocusContainer(widget);

        assertFalse(ForgeContainerSidebar.isTextInputFocused(container));
    }

    @Test
    void keyPressListenerRunsBeforeHostScreenHandlersAndCanceledSubscribers() throws Exception {
        Method method = ForgeWorkspaceClient.GameBus.class.getDeclaredMethod(
                "onKeyPressed",
                ScreenEvent.KeyPressed.Pre.class
        );

        SubscribeEvent annotation = method.getAnnotation(SubscribeEvent.class);

        assertNotNull(annotation);
        assertEquals(EventPriority.HIGHEST, annotation.priority());
        assertTrue(annotation.receiveCanceled());
    }

    @Test
    void emiTextGuardTreatsLettersButNotHotbarDigitsAsPlainTextKeys() {
        assertTrue(ForgeSlotTextInputKeyGuard.isPlainTextKey(GLFW.GLFW_KEY_R, 0));
        assertTrue(ForgeSlotTextInputKeyGuard.isPlainTextKey(GLFW.GLFW_KEY_U, 0));
        assertFalse(ForgeSlotTextInputKeyGuard.isPlainTextKey(GLFW.GLFW_KEY_1, 0));
        assertFalse(ForgeSlotTextInputKeyGuard.isPlainTextKey(GLFW.GLFW_KEY_KP_1, 0));
        assertFalse(ForgeSlotTextInputKeyGuard.isPlainTextKey(GLFW.GLFW_KEY_R, GLFW.GLFW_MOD_CONTROL));
    }

    @Test
    void activeOverlayMakesWholeScreenInteractiveForContextMenus() {
        assertTrue(ForgeContainerSidebar.insideInteractiveSurface(
                500,
                120,
                true,
                8,
                12,
                268,
                220,
                new ForgeWorkspaceSurface.RecentsPanelBounds(160, 4, 120, 72),
                new ForgeWorkspaceSurface.TaskPanelBounds(410, 20, 140, 180)));
    }

    @Test
    void inactiveOverlayStillLimitsSidebarInputToVisibleSlotSurfaces() {
        assertFalse(ForgeContainerSidebar.insideInteractiveSurface(
                350,
                120,
                false,
                8,
                12,
                268,
                220,
                new ForgeWorkspaceSurface.RecentsPanelBounds(160, 4, 120, 72),
                new ForgeWorkspaceSurface.TaskPanelBounds(410, 20, 140, 180)));
        assertTrue(ForgeContainerSidebar.insideInteractiveSurface(
                40,
                80,
                false,
                8,
                12,
                268,
                220,
                null,
                null));
        assertTrue(ForgeContainerSidebar.insideInteractiveSurface(
                170,
                20,
                false,
                8,
                12,
                268,
                220,
                new ForgeWorkspaceSurface.RecentsPanelBounds(160, 4, 120, 72),
                null));
        assertTrue(ForgeContainerSidebar.insideInteractiveSurface(
                420,
                40,
                false,
                8,
                12,
                268,
                220,
                null,
                new ForgeWorkspaceSurface.TaskPanelBounds(410, 20, 140, 180)));
    }

    private static final class FocusableWidget implements GuiEventListener {
        private boolean focused;

        private FocusableWidget(boolean focused) {
            this.focused = focused;
        }

        @Override
        public void setFocused(boolean focused) {
            this.focused = focused;
        }

        @Override
        public boolean isFocused() {
            return focused;
        }
    }

    private static final class FocusContainer implements ContainerEventHandler {
        private final GuiEventListener focused;
        private boolean dragging;

        private FocusContainer(GuiEventListener focused) {
            this.focused = focused;
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of(focused);
        }

        @Override
        public boolean isDragging() {
            return dragging;
        }

        @Override
        public void setDragging(boolean dragging) {
            this.dragging = dragging;
        }

        @Override
        public GuiEventListener getFocused() {
            return focused;
        }

        @Override
        public void setFocused(GuiEventListener focused) {
            throw new UnsupportedOperationException("test fixture is immutable");
        }
    }
}
