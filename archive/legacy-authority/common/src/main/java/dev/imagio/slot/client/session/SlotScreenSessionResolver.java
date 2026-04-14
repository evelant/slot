package dev.imagio.slot.client.session;

import dev.imagio.slot.client.screen.SlotCarriedInventoryScreen;
import dev.imagio.slot.client.screen.container.SlotInventoryWorkspaceScreen;
import dev.imagio.slot.session.InventoryHostDescriptor;
import dev.imagio.slot.session.StorageViewResolver;
import dev.imagio.slot.session.SlotSessionDescriptor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.Map;
import java.util.WeakHashMap;

public final class SlotScreenSessionResolver {
    private static final Map<Screen, CachedScreenSession> SCREEN_CACHE = new WeakHashMap<>();

    private SlotScreenSessionResolver() {
    }

    public static SlotScreenSession resolve(Screen screen, LocalPlayer player) {
        if (screen == null || screen instanceof ChatScreen) {
            return new SlotScreenSession(SlotSessionKind.GENERAL, screenClassName(screen), null);
        }
        if (screen instanceof SlotInventoryWorkspaceScreen<?>) {
            return new SlotScreenSession(SlotSessionKind.SLOT_WORKSPACE, screenClassName(screen), null);
        }
        if (screen instanceof SlotCarriedInventoryScreen) {
            return new SlotScreenSession(SlotSessionKind.SLOT_CARRIED, screenClassName(screen), null);
        }
        if (!(screen instanceof AbstractContainerScreen<?> containerScreen)) {
            return new SlotScreenSession(SlotSessionKind.GENERAL, screenClassName(screen), null);
        }
        if (player == null || containerScreen.getMenu() == null || containerScreen.getMenu() == player.inventoryMenu) {
            return new SlotScreenSession(SlotSessionKind.PLAYER_INVENTORY, screenClassName(screen), null);
        }
        return resolveCachedContainerSession(containerScreen, player);
    }

    public static SlotScreenSession resolveContainerScreen(AbstractContainerScreen<?> screen, LocalPlayer player) {
        if (screen == null) {
            return new SlotScreenSession(SlotSessionKind.GENERAL, "", null);
        }
        return resolve(screen, player);
    }

    public static SlotSessionDescriptor resolveDescriptor(Screen screen, LocalPlayer player) {
        return resolve(screen, player).descriptor();
    }

    public static SlotScreenSession resolveActiveCarriedMenu(Minecraft minecraft, Component title, Screen currentScreen) {
        if (minecraft == null || minecraft.player == null) {
            return null;
        }

        AbstractContainerMenu activeMenu = minecraft.player.containerMenu;
        if (activeMenu == null || activeMenu == minecraft.player.inventoryMenu) {
            return null;
        }

        SlotScreenSession session = resolveMenu(
                title,
                activeMenu,
                minecraft.player.getInventory(),
                currentScreen == null ? "" : currentScreen.getClass().getName()
        );
        if (session.kind() != SlotSessionKind.CARRIED_CONTAINER || !session.hasStorageView()) {
            return null;
        }
        return session;
    }

    public static SlotSessionDescriptor resolveMenuDescriptor(
            Component title,
            AbstractContainerMenu menu,
            Inventory playerInventory,
            String screenClassName
    ) {
        return resolveMenu(title, menu, playerInventory, screenClassName).descriptor();
    }

    public static boolean recordsRecentLoot(Minecraft minecraft, LocalPlayer player) {
        if (minecraft == null) {
            return false;
        }
        return resolve(minecraft.screen, player).recordsRecentLoot();
    }

    static void clearCache() {
        synchronized (SCREEN_CACHE) {
            SCREEN_CACHE.clear();
        }
    }

    public static SlotScreenSession resolveMenu(
            Component title,
            AbstractContainerMenu menu,
            Inventory playerInventory,
            String screenClassName
    ) {
        if (menu == null || playerInventory == null) {
            return new SlotScreenSession(SlotSessionKind.NON_STORAGE_CONTAINER, screenClassName, null);
        }

        InventoryHostDescriptor host = StorageViewResolver.resolve(
                title,
                menu,
                playerInventory,
                screenClassName,
                false,
                false,
                false
        );
        if (host != null) {
            SlotSessionKind kind = host.hasExternalHostStorage()
                    ? SlotSessionKind.EXTERNAL_CONTAINER
                    : host.hasCarriedHostStorage()
                    ? SlotSessionKind.CARRIED_CONTAINER
                    : SlotSessionKind.NON_STORAGE_CONTAINER;
            host = StorageViewResolver.resolve(
                    title,
                    menu,
                    playerInventory,
                    screenClassName,
                    false,
                    kind.recordsRecentLoot(),
                    kind == SlotSessionKind.CARRIED_CONTAINER
            );
            return new SlotScreenSession(
                    kind,
                    screenClassName,
                    host
            );
        }

        return new SlotScreenSession(SlotSessionKind.NON_STORAGE_CONTAINER, screenClassName, null);
    }

    private static SlotScreenSession resolveCachedContainerSession(AbstractContainerScreen<?> screen, LocalPlayer player) {
        synchronized (SCREEN_CACHE) {
            CachedScreenSession cached = SCREEN_CACHE.get(screen);
            if (cached != null && cached.menu() == screen.getMenu() && cached.playerInventory() == player.getInventory()) {
                return cached.session();
            }
            SlotScreenSession session = resolveMenu(
                    screen.getTitle(),
                    screen.getMenu(),
                    player.getInventory(),
                    screen.getClass().getName()
            );
            SCREEN_CACHE.put(screen, new CachedScreenSession(screen.getMenu(), player.getInventory(), session));
            return session;
        }
    }

    private static String screenClassName(Screen screen) {
        return screen == null ? "" : screen.getClass().getName();
    }

    private record CachedScreenSession(
            AbstractContainerMenu menu,
            Inventory playerInventory,
            SlotScreenSession session
    ) {
    }
}
