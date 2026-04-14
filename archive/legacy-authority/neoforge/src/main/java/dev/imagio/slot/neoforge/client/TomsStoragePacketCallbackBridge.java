package dev.imagio.slot.neoforge.client;

import dev.imagio.slot.SlotDebugLog;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.WeakHashMap;

final class TomsStoragePacketCallbackBridge {
    private static final String TOMS_STORAGE_MENU_CLASS = "com.tom.storagemod.menu.StorageTerminalMenu";
    private static final Map<AbstractContainerMenu, Runnable> DETACHED_PACKET_HANDLERS = new WeakHashMap<>();

    private TomsStoragePacketCallbackBridge() {
    }

    static void detach(AbstractContainerScreen<?> screen) {
        if (screen == null) {
            return;
        }

        AbstractContainerMenu menu = screen.getMenu();
        if (!isTomsStorageMenu(menu)) {
            return;
        }

        try {
            Field onPacketField = findField(menu.getClass(), "onPacket");
            if (onPacketField == null) {
                return;
            }

            Object callback = onPacketField.get(menu);
            if (callback instanceof Runnable runnable) {
                DETACHED_PACKET_HANDLERS.putIfAbsent(menu, runnable);
                onPacketField.set(menu, null);
                SlotDebugLog.log(
                        "Detached Tom's terminal packet callback while SLOT owns the screen: menu={} screen={}",
                        menu.getClass().getName(),
                        screen.getClass().getName()
                );
            }
        } catch (IllegalAccessException exception) {
            SlotDebugLog.log(
                    "Failed to detach Tom's terminal packet callback: menu={} error={}",
                    menu.getClass().getName(),
                    exception.getClass().getName()
            );
        }
    }

    static void restore(AbstractContainerScreen<?> screen) {
        if (screen == null) {
            return;
        }

        AbstractContainerMenu menu = screen.getMenu();
        if (!isTomsStorageMenu(menu)) {
            return;
        }

        Runnable callback = DETACHED_PACKET_HANDLERS.get(menu);
        if (callback == null) {
            return;
        }

        try {
            Field onPacketField = findField(menu.getClass(), "onPacket");
            if (onPacketField == null) {
                return;
            }

            onPacketField.set(menu, callback);
            SlotDebugLog.log(
                    "Restored Tom's terminal packet callback for vanilla screen: menu={} screen={}",
                    menu.getClass().getName(),
                    screen.getClass().getName()
            );
        } catch (IllegalAccessException exception) {
            SlotDebugLog.log(
                    "Failed to restore Tom's terminal packet callback: menu={} error={}",
                    menu.getClass().getName(),
                    exception.getClass().getName()
            );
        }
    }

    private static boolean isTomsStorageMenu(AbstractContainerMenu menu) {
        if (menu == null) {
            return false;
        }
        Class<?> type = menu.getClass();
        while (type != null) {
            if (TOMS_STORAGE_MENU_CLASS.equals(type.getName())) {
                return true;
            }
            type = type.getSuperclass();
        }
        return false;
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
