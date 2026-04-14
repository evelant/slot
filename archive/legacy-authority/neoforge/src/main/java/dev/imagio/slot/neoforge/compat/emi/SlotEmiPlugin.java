package dev.imagio.slot.neoforge.compat.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.widget.Bounds;
import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.client.SlotClientCompat;
import dev.imagio.slot.client.screen.SlotCarriedInventoryScreen;
import dev.imagio.slot.client.screen.SlotPanelBounds;
import dev.imagio.slot.client.screen.SlotPanelScreen;
import dev.imagio.slot.client.screen.container.SlotInventoryWorkspaceScreen;
import dev.imagio.slot.client.screen.debug.SlotDebugInventoryScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@EmiEntrypoint
public final class SlotEmiPlugin implements EmiPlugin {
    private static final int EMI_TOP_SAFE_AREA = 12;
    private static final int EMI_LEFT_SAFE_AREA = 12;
    private static final Map<String, SlotPanelBounds> LAST_LOGGED_BOUNDS = new ConcurrentHashMap<>();

    @Override
    public void register(EmiRegistry registry) {
        SlotClientCompat.setEmiRuntimeEnabled(true);
        SlotDebugLog.log("Registering EMI exclusion support for SLOT panel screens");
        TomsStorageEmiRecipeHandler.register(registry);
        registerScreenBoundsProvider(registry, SlotCarriedInventoryScreen.class);
        registerScreenBoundsProvider(registry, SlotDebugInventoryScreen.class);
        registerScreenBoundsProvider(registry, SlotInventoryWorkspaceScreen.class);
        registry.addGenericExclusionArea((screen, consumer) -> {
            if (!(screen instanceof SlotPanelScreen slotPanelScreen)) {
                return;
            }

            SlotPanelBounds bounds = slotPanelScreen.slotPanelBounds();
            if (bounds.width() <= 0 || bounds.height() <= 0) {
                return;
            }

            String screenKey = screen.getClass().getName();
            SlotPanelBounds lastLoggedBounds = LAST_LOGGED_BOUNDS.put(screenKey, bounds);
            if (!bounds.equals(lastLoggedBounds)) {
                SlotDebugLog.log("Providing EMI exclusion bounds x={} y={} w={} h={} for screen={}", bounds.x(), bounds.y(), bounds.width(), bounds.height(), screenKey);
            }
            consumer.accept(new Bounds(bounds.x(), bounds.y(), bounds.width(), bounds.height()));
            consumer.accept(new Bounds(0, 0, screen.width, EMI_TOP_SAFE_AREA));
            consumer.accept(new Bounds(0, 0, EMI_LEFT_SAFE_AREA, screen.height));
        });
    }

    public static void ensureTomsRecipeHandler(AbstractContainerMenu menu) {
        TomsStorageEmiRecipeHandler.ensurePreferredHandler(menu);
    }

    private void registerScreenBoundsProvider(EmiRegistry registry, Class<?> screenClass) {
        try {
            Class<?> providerClass = Class.forName("dev.emi.emi.api.EmiScreenBoundsProvider");
            Method addProvider = registry.getClass().getMethod("addScreenBoundsProvider", Class.class, providerClass);
            Object provider = Proxy.newProxyInstance(
                    providerClass.getClassLoader(),
                    new Class<?>[] {providerClass},
                    (proxy, method, args) -> {
                        if ("getBounds".equals(method.getName()) && args != null && args.length == 1 && args[0] instanceof SlotPanelScreen slotPanelScreen) {
                            SlotPanelBounds bounds = slotPanelScreen.slotPanelBounds();
                            return new Bounds(bounds.x(), bounds.y(), bounds.width(), bounds.height());
                        }
                        if ("toString".equals(method.getName())) {
                            return "SLOT EMI ScreenBoundsProvider";
                        }
                        return null;
                    }
            );
            addProvider.invoke(registry, screenClass, provider);
            SlotDebugLog.log("Registered EMI screen bounds provider for {}", screenClass.getName());
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
            SlotDebugLog.log("EMI screen bounds provider API unavailable; falling back to exclusion areas only");
        } catch (IllegalAccessException | InvocationTargetException exception) {
            SlotDebugLog.log("Failed to register EMI screen bounds provider for {}: {}", screenClass.getName(), exception.toString());
        }
    }
}
