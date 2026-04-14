package dev.imagio.slot.client.screen;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.client.SlotClientCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class EmiLayoutSync {
    private static final Method FORCE_RECALCULATE = resolveForceRecalculate();
    private static final EmiPanelAccess PANEL_ACCESS = resolvePanelAccess();
    private static final EmiWidgetAccess WIDGET_ACCESS = resolveWidgetAccess();
    private static final int EMI_WIDGET_ANCHOR_GAP = 8;
    private static final int EMI_WIDGET_RIGHT_MARGIN = 2;
    private static final int EMI_WIDGET_BUTTON_WIDTH = 20;
    private static final int EMI_WIDGET_BUTTON_GAP = 2;
    private static final int EMI_WIDGET_GROUP_GAP = 6;
    private static final int EMI_SEARCH_DEFAULT_WIDTH = 160;
    private static final int EMI_SEARCH_MIN_WIDTH = 120;

    private EmiLayoutSync() {
    }

    public static void refreshIfPresent(String reason) {
        if (!SlotClientCompat.hasEmi() || FORCE_RECALCULATE == null) {
            return;
        }
        try {
            FORCE_RECALCULATE.invoke(null);
            SlotDebugLog.log("Requested EMI layout recalculation: {}", reason);
            alignWidgets(reason);
            logPanelBounds(reason);
        } catch (ReflectiveOperationException exception) {
            SlotDebugLog.log("Failed to request EMI layout recalculation for {}: {}", reason, exception.toString());
        }
    }

    public static boolean hasSearchWidget() {
        if (!SlotClientCompat.hasEmi() || WIDGET_ACCESS == null) {
            return false;
        }
        try {
            return WIDGET_ACCESS.searchField.get(null) != null;
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    public static String currentSearchQuery() {
        Object widget = searchWidget();
        if (widget == null) {
            return "";
        }
        try {
            Method getValue = findMethod(widget.getClass(), "getValue");
            if (getValue != null) {
                Object value = getValue.invoke(widget);
                return value instanceof String string ? string : "";
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return "";
    }

    public static boolean isSearchFocused() {
        Object widget = searchWidget();
        if (widget == null) {
            return false;
        }
        try {
            Method isFocused = findMethod(widget.getClass(), "isFocused");
            if (isFocused != null) {
                Object value = isFocused.invoke(widget);
                return value instanceof Boolean bool && bool;
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return false;
    }

    public static void setSearchQuery(String query) {
        Object widget = searchWidget();
        if (widget == null) {
            return;
        }
        try {
            Method setValue = findMethod(widget.getClass(), "setValue", String.class);
            if (setValue != null) {
                setValue.invoke(widget, query == null ? "" : query);
            }
        } catch (ReflectiveOperationException exception) {
            SlotDebugLog.log("Failed to set EMI search query: {}", exception.toString());
        }
    }

    private static Object searchWidget() {
        if (!SlotClientCompat.hasEmi() || WIDGET_ACCESS == null) {
            return null;
        }
        try {
            return WIDGET_ACCESS.searchField.get(null);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static Method resolveForceRecalculate() {
        try {
            Class<?> managerClass = Class.forName("dev.emi.emi.screen.EmiScreenManager");
            Method method = managerClass.getMethod("forceRecalculate");
            method.setAccessible(true);
            return method;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static void logPanelBounds(String reason) {
        if (PANEL_ACCESS == null) {
            return;
        }
        try {
            String leftBounds = PANEL_ACCESS.describe("LEFT");
            String rightBounds = PANEL_ACCESS.describe("RIGHT");
            String topBounds = PANEL_ACCESS.describe("TOP");
            String bottomBounds = PANEL_ACCESS.describe("BOTTOM");
            SlotDebugLog.log("EMI panel bounds after {}: left={} right={} top={} bottom={}", reason, leftBounds, rightBounds, topBounds, bottomBounds);
        } catch (ReflectiveOperationException exception) {
            SlotDebugLog.log("Failed to inspect EMI panel bounds after {}: {}", reason, exception.toString());
        }
    }

    private static void alignWidgets(String reason) {
        if (PANEL_ACCESS == null || WIDGET_ACCESS == null) {
            return;
        }
        Screen screen = Minecraft.getInstance().screen;
        if (screen == null) {
            return;
        }
        try {
            BoundsInfo rightBounds = PANEL_ACCESS.bounds("RIGHT");
            if (rightBounds == null || rightBounds.width() <= 0 || rightBounds.height() <= 0) {
                return;
            }
            BoundsInfo bottomBounds = PANEL_ACCESS.bounds("BOTTOM");

            Object searchWidget = WIDGET_ACCESS.searchField.get(null);
            Object emiButton = WIDGET_ACCESS.emiField.get(null);
            Object treeButton = WIDGET_ACCESS.treeField.get(null);
            if (searchWidget == null) {
                return;
            }

            int anchorLeft = rightBounds.left();
            if (bottomBounds != null && bottomBounds.width() > 0 && bottomBounds.height() > 0) {
                anchorLeft = Math.min(anchorLeft, bottomBounds.left());
            }
            int widgetRight = Math.min(screen.width - EMI_WIDGET_RIGHT_MARGIN, anchorLeft - EMI_WIDGET_ANCHOR_GAP);
            int widgetLeft = 0;
            int buttonCount = countVisibleButton(emiButton) + countVisibleButton(treeButton);
            int buttonsWidth = buttonCount == 0 ? 0
                    : buttonCount * EMI_WIDGET_BUTTON_WIDTH + (buttonCount - 1) * EMI_WIDGET_BUTTON_GAP;
            int availableWidth = Math.max(0, widgetRight - widgetLeft);
            int maxSearchWidth = availableWidth - buttonsWidth - (buttonCount == 0 ? 0 : EMI_WIDGET_GROUP_GAP);
            if (maxSearchWidth <= 0) {
                return;
            }
            int searchWidth = Math.min(EMI_SEARCH_DEFAULT_WIDTH, maxSearchWidth);
            if (searchWidth < EMI_SEARCH_MIN_WIDTH) {
                searchWidth = maxSearchWidth;
            }

            int searchX;
            int buttonX = widgetLeft;
            if (buttonCount == 0) {
                searchX = widgetRight - searchWidth;
            } else {
                int usedWidth = buttonsWidth + EMI_WIDGET_GROUP_GAP + searchWidth;
                if (usedWidth > availableWidth) {
                    searchWidth = Math.max(0, availableWidth - buttonsWidth - EMI_WIDGET_GROUP_GAP);
                    if (searchWidth <= 0) {
                        return;
                    }
                }
                searchX = widgetRight - searchWidth;
                buttonX = Math.max(widgetLeft, searchX - EMI_WIDGET_GROUP_GAP - buttonsWidth);
            }

            setX(searchWidget, searchX);
            setY(searchWidget, screen.height - 21);
            setWidth(searchWidget, searchWidth);

            int nextButtonX = buttonX;
            if (countVisibleButton(emiButton) > 0) {
                setX(emiButton, nextButtonX);
                setY(emiButton, screen.height - 22);
                nextButtonX += EMI_WIDGET_BUTTON_WIDTH + EMI_WIDGET_BUTTON_GAP;
            }
            if (countVisibleButton(treeButton) > 0) {
                setX(treeButton, nextButtonX);
                setY(treeButton, screen.height - 22);
            }

            SlotDebugLog.log(
                    "Aligned EMI widgets after {}: rightPanelLeft={} rightPanelRight={} bottomPanelLeft={} bottomPanelRight={} widgetRight={} searchX={} searchWidth={} buttonStartX={} buttonCount={}",
                    reason,
                    rightBounds.left(),
                    rightBounds.right(),
                    bottomBounds == null ? -1 : bottomBounds.left(),
                    bottomBounds == null ? -1 : bottomBounds.right(),
                    widgetRight,
                    searchX,
                    searchWidth,
                    buttonX,
                    buttonCount
            );
        } catch (ReflectiveOperationException exception) {
            SlotDebugLog.log("Failed to align EMI widgets after {}: {}", reason, exception.toString());
        }
    }

    private static EmiPanelAccess resolvePanelAccess() {
        try {
            Class<?> managerClass = Class.forName("dev.emi.emi.screen.EmiScreenManager");
            Class<?> sidebarSideClass = Class.forName("dev.emi.emi.config.SidebarSide");
            Method getPanelFor = managerClass.getMethod("getPanelFor", sidebarSideClass);
            Method getBounds = Class.forName("dev.emi.emi.screen.EmiScreenManager$SidebarPanel").getMethod("getBounds");
            Method left = Class.forName("dev.emi.emi.api.widget.Bounds").getMethod("left");
            Method top = Class.forName("dev.emi.emi.api.widget.Bounds").getMethod("top");
            Method right = Class.forName("dev.emi.emi.api.widget.Bounds").getMethod("right");
            Method bottom = Class.forName("dev.emi.emi.api.widget.Bounds").getMethod("bottom");
            return new EmiPanelAccess(sidebarSideClass, getPanelFor, getBounds, left, top, right, bottom);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static EmiWidgetAccess resolveWidgetAccess() {
        try {
            Class<?> managerClass = Class.forName("dev.emi.emi.screen.EmiScreenManager");
            Field searchField = managerClass.getField("search");
            Field emiField = managerClass.getField("emi");
            Field treeField = managerClass.getField("tree");
            return new EmiWidgetAccess(searchField, emiField, treeField);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static int countVisibleButton(Object widget) throws ReflectiveOperationException {
        if (widget == null) {
            return 0;
        }
        Field visibleField = findField(widget.getClass(), "visible");
        if (visibleField != null && visibleField.getType() == boolean.class) {
            return visibleField.getBoolean(widget) ? 1 : 0;
        }
        return 1;
    }

    private static void setX(Object widget, int x) throws ReflectiveOperationException {
        if (!invokeIntSetter(widget, "setX", x)) {
            Field field = findField(widget.getClass(), "x");
            if (field != null) {
                field.setInt(widget, x);
            }
        }
    }

    private static void setY(Object widget, int y) throws ReflectiveOperationException {
        if (!invokeIntSetter(widget, "setY", y)) {
            Field field = findField(widget.getClass(), "y");
            if (field != null) {
                field.setInt(widget, y);
            }
        }
    }

    private static void setWidth(Object widget, int width) throws ReflectiveOperationException {
        if (!invokeIntSetter(widget, "setWidth", width)) {
            Field field = findField(widget.getClass(), "width");
            if (field != null) {
                field.setInt(widget, width);
            }
        }
    }

    private static boolean invokeIntSetter(Object widget, String methodName, int value) throws ReflectiveOperationException {
        if (widget == null) {
            return false;
        }
        Method method = findMethod(widget.getClass(), methodName, int.class);
        if (method == null) {
            return false;
        }
        method.invoke(widget, value);
        return true;
    }

    private static Method findMethod(Class<?> type, String name, Class<?>... parameterTypes) {
        Class<?> current = type;
        while (current != null) {
            try {
                Method method = current.getDeclaredMethod(name, parameterTypes);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
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

    private record EmiPanelAccess(
            Class<?> sidebarSideClass,
            Method getPanelFor,
            Method getBounds,
            Method left,
            Method top,
            Method right,
            Method bottom
    ) {
        private String describe(String sideName) throws ReflectiveOperationException {
            BoundsInfo bounds = bounds(sideName);
            if (bounds == null) {
                return "empty";
            }
            return bounds.left() + "," + bounds.top() + " -> " + bounds.right() + "," + bounds.bottom();
        }

        private BoundsInfo bounds(String sideName) throws ReflectiveOperationException {
            Object side = enumConstant(sidebarSideClass, sideName);
            if (side == null) {
                return null;
            }
            Object panel = getPanelFor.invoke(null, side);
            if (panel == null) {
                return null;
            }
            Object bounds = getBounds.invoke(panel);
            if (bounds == null) {
                return null;
            }
            return new BoundsInfo(
                    ((Number) left.invoke(bounds)).intValue(),
                    ((Number) top.invoke(bounds)).intValue(),
                    ((Number) right.invoke(bounds)).intValue(),
                    ((Number) bottom.invoke(bounds)).intValue()
            );
        }

        private static Object enumConstant(Class<?> enumType, String constantName) {
            Object[] constants = enumType.getEnumConstants();
            if (constants == null) {
                return null;
            }
            for (Object constant : constants) {
                Enum<?> enumConstant = (Enum<?>) constant;
                if (enumConstant.name().equals(constantName)) {
                    return enumConstant;
                }
            }
            return null;
        }
    }

    private record EmiWidgetAccess(Field searchField, Field emiField, Field treeField) {
    }

    private record BoundsInfo(int left, int top, int right, int bottom) {
        private int width() {
            return Math.max(0, right - left);
        }

        private int height() {
            return Math.max(0, bottom - top);
        }
    }
}
