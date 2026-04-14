package dev.imagio.slot.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class ReflectiveContainerRenderHooks {
    private final Object eventBus;
    private final Method postMethod;
    private final Constructor<?> backgroundConstructor;
    private final Constructor<?> foregroundConstructor;
    private final Field leftPosField;
    private final Field topPosField;

    private ReflectiveContainerRenderHooks(
            Object eventBus,
            Method postMethod,
            Constructor<?> backgroundConstructor,
            Constructor<?> foregroundConstructor,
            Field leftPosField,
            Field topPosField
    ) {
        this.eventBus = eventBus;
        this.postMethod = postMethod;
        this.backgroundConstructor = backgroundConstructor;
        this.foregroundConstructor = foregroundConstructor;
        this.leftPosField = leftPosField;
        this.topPosField = topPosField;
    }

    public static ReflectiveContainerRenderHooks create() {
        try {
            Class<?> neoForgeClass = Class.forName("net.neoforged.neoforge.common.NeoForge");
            Object eventBus = neoForgeClass.getField("EVENT_BUS").get(null);
            Method postMethod = null;
            for (Method candidate : eventBus.getClass().getMethods()) {
                if (candidate.getName().equals("post") && candidate.getParameterCount() == 1) {
                    postMethod = candidate;
                    break;
                }
            }
            if (postMethod == null) {
                return new ReflectiveContainerRenderHooks(null, null, null, null, null, null);
            }

            Constructor<?> backgroundConstructor = Class
                    .forName("net.neoforged.neoforge.client.event.ContainerScreenEvent$Render$Background")
                    .getConstructor(AbstractContainerScreen.class, GuiGraphics.class, int.class, int.class);
            Constructor<?> foregroundConstructor = Class
                    .forName("net.neoforged.neoforge.client.event.ContainerScreenEvent$Render$Foreground")
                    .getConstructor(AbstractContainerScreen.class, GuiGraphics.class, int.class, int.class);
            Field leftPosField = findField(AbstractContainerScreen.class, "leftPos");
            Field topPosField = findField(AbstractContainerScreen.class, "topPos");
            return new ReflectiveContainerRenderHooks(eventBus, postMethod, backgroundConstructor, foregroundConstructor, leftPosField, topPosField);
        } catch (ReflectiveOperationException ignored) {
            return new ReflectiveContainerRenderHooks(null, null, null, null, null, null);
        }
    }

    public void postBackground(AbstractContainerScreen<?> screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        post(backgroundConstructor, screen, guiGraphics, mouseX, mouseY);
    }

    public void postForeground(AbstractContainerScreen<?> screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (eventBus == null || postMethod == null || foregroundConstructor == null) {
            return;
        }
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(resolveLeftPos(screen), resolveTopPos(screen), 0.0F);
        try {
            Object event = foregroundConstructor.newInstance(screen, guiGraphics, mouseX, mouseY);
            postMethod.invoke(eventBus, event);
        } catch (ReflectiveOperationException ignored) {
        } finally {
            guiGraphics.pose().popPose();
        }
    }

    private void post(Constructor<?> constructor, AbstractContainerScreen<?> screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (eventBus == null || postMethod == null || constructor == null) {
            return;
        }
        try {
            Object event = constructor.newInstance(screen, guiGraphics, mouseX, mouseY);
            postMethod.invoke(eventBus, event);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    private int resolveLeftPos(AbstractContainerScreen<?> screen) {
        return resolveIntField(leftPosField, screen);
    }

    private int resolveTopPos(AbstractContainerScreen<?> screen) {
        return resolveIntField(topPosField, screen);
    }

    private int resolveIntField(Field field, Object target) {
        if (field == null || target == null) {
            return 0;
        }
        try {
            return field.getInt(target);
        } catch (IllegalAccessException ignored) {
            return 0;
        }
    }

    private static Field findField(Class<?> type, String name) {
        Class<?> current = type;
        while (current != null) {
            try {
                Field field = current.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
    }
}
