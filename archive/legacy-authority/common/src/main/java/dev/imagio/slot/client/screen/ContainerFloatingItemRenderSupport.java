package dev.imagio.slot.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Field;
import java.util.Set;

public final class ContainerFloatingItemRenderSupport {
    private static final Field DRAGGING_ITEM_FIELD = findField("draggingItem");
    private static final Field IS_SPLITTING_STACK_FIELD = findField("isSplittingStack");
    private static final Field IS_QUICK_CRAFTING_FIELD = findField("isQuickCrafting");
    private static final Field QUICK_CRAFT_SLOTS_FIELD = findField("quickCraftSlots");
    private static final Field QUICK_CRAFTING_REMAINDER_FIELD = findField("quickCraftingRemainder");
    private static final Field SNAPBACK_ITEM_FIELD = findField("snapbackItem");
    private static final Field SNAPBACK_END_FIELD = findField("snapbackEnd");
    private static final Field SNAPBACK_START_X_FIELD = findField("snapbackStartX");
    private static final Field SNAPBACK_START_Y_FIELD = findField("snapbackStartY");
    private static final Field SNAPBACK_TIME_FIELD = findField("snapbackTime");

    private ContainerFloatingItemRenderSupport() {
    }

    public static boolean hasRenderableFloatingItems(AbstractContainerScreen<?> screen, ItemStack carriedStack) {
        return !visibleFloatingStack(screen, carriedStack).isEmpty() || !readStack(screen, SNAPBACK_ITEM_FIELD).isEmpty();
    }

    public static ItemStack visibleFloatingStack(AbstractContainerScreen<?> screen, ItemStack carriedStack) {
        ItemStack draggingItem = readStack(screen, DRAGGING_ITEM_FIELD);
        ItemStack baseStack = draggingItem.isEmpty() ? copyOrEmpty(carriedStack) : draggingItem;
        if (baseStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        if (!draggingItem.isEmpty() && readBoolean(screen, IS_SPLITTING_STACK_FIELD)) {
            return baseStack.copyWithCount(Mth.ceil(baseStack.getCount() / 2.0F));
        }

        if (draggingItem.isEmpty()
                && readBoolean(screen, IS_QUICK_CRAFTING_FIELD)
                && quickCraftSlotCount(screen) > 1) {
            return baseStack.copyWithCount(readInt(screen, QUICK_CRAFTING_REMAINDER_FIELD));
        }

        return baseStack;
    }

    public static void renderFloatingItems(
            AbstractContainerScreen<?> screen,
            Font font,
            GuiGraphics guiGraphics,
            int leftPos,
            int topPos,
            ItemStack carriedStack,
            int mouseX,
            int mouseY
    ) {
        if (screen == null || font == null || guiGraphics == null) {
            return;
        }

        RenderSystem.disableDepthTest();

        FloatingItem floatingItem = resolveFloatingItem(screen, carriedStack);
        if (!floatingItem.stack().isEmpty()) {
            renderFloatingItem(guiGraphics, font, floatingItem.stack(), mouseX - 8, mouseY - floatingItem.yOffset(), floatingItem.text(), floatingItem.dragging());
        }

        renderSnapbackItem(screen, font, guiGraphics, leftPos, topPos);

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static FloatingItem resolveFloatingItem(AbstractContainerScreen<?> screen, ItemStack carriedStack) {
        ItemStack draggingItem = readStack(screen, DRAGGING_ITEM_FIELD);
        ItemStack baseStack = draggingItem.isEmpty() ? copyOrEmpty(carriedStack) : draggingItem;
        if (baseStack.isEmpty()) {
            return FloatingItem.empty();
        }

        if (!draggingItem.isEmpty() && readBoolean(screen, IS_SPLITTING_STACK_FIELD)) {
            return new FloatingItem(baseStack.copyWithCount(Mth.ceil(baseStack.getCount() / 2.0F)), null, true, 16);
        }

        if (draggingItem.isEmpty()
                && readBoolean(screen, IS_QUICK_CRAFTING_FIELD)
                && quickCraftSlotCount(screen) > 1) {
            ItemStack quickCraftStack = baseStack.copyWithCount(readInt(screen, QUICK_CRAFTING_REMAINDER_FIELD));
            String text = quickCraftStack.isEmpty() ? ChatFormatting.YELLOW + "0" : null;
            return new FloatingItem(quickCraftStack, text, false, 8);
        }

        return new FloatingItem(baseStack, null, !draggingItem.isEmpty(), draggingItem.isEmpty() ? 8 : 16);
    }

    private static void renderSnapbackItem(
            AbstractContainerScreen<?> screen,
            Font font,
            GuiGraphics guiGraphics,
            int leftPos,
            int topPos
    ) {
        ItemStack snapbackItem = readStack(screen, SNAPBACK_ITEM_FIELD);
        if (snapbackItem.isEmpty()) {
            return;
        }

        Slot snapbackEnd = readSlot(screen, SNAPBACK_END_FIELD);
        if (snapbackEnd == null) {
            setStack(screen, SNAPBACK_ITEM_FIELD, ItemStack.EMPTY);
            return;
        }

        float progress = (float) (Util.getMillis() - readLong(screen, SNAPBACK_TIME_FIELD)) / 100.0F;
        if (progress >= 1.0F) {
            progress = 1.0F;
            setStack(screen, SNAPBACK_ITEM_FIELD, ItemStack.EMPTY);
        }

        int deltaX = snapbackEnd.x - readInt(screen, SNAPBACK_START_X_FIELD);
        int deltaY = snapbackEnd.y - readInt(screen, SNAPBACK_START_Y_FIELD);
        int x = leftPos + readInt(screen, SNAPBACK_START_X_FIELD) + (int) (deltaX * progress);
        int y = topPos + readInt(screen, SNAPBACK_START_Y_FIELD) + (int) (deltaY * progress);
        renderFloatingItem(guiGraphics, font, snapbackItem, x, y, null, false);
    }

    private static void renderFloatingItem(
            GuiGraphics guiGraphics,
            Font font,
            ItemStack stack,
            int x,
            int y,
            String text,
            boolean dragging
    ) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, 232.0F);
        guiGraphics.renderItem(stack, x, y);
        guiGraphics.renderItemDecorations(font, stack, x, y - (dragging ? 8 : 0), text);
        guiGraphics.pose().popPose();
    }

    private static int quickCraftSlotCount(AbstractContainerScreen<?> screen) {
        Object value = read(screen, QUICK_CRAFT_SLOTS_FIELD);
        if (value instanceof Set<?> slots) {
            return slots.size();
        }
        return 0;
    }

    private static ItemStack copyOrEmpty(ItemStack stack) {
        return stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
    }

    private static ItemStack readStack(AbstractContainerScreen<?> screen, Field field) {
        Object value = read(screen, field);
        return value instanceof ItemStack stack ? stack : ItemStack.EMPTY;
    }

    private static Slot readSlot(AbstractContainerScreen<?> screen, Field field) {
        Object value = read(screen, field);
        return value instanceof Slot slot ? slot : null;
    }

    private static boolean readBoolean(AbstractContainerScreen<?> screen, Field field) {
        Object value = read(screen, field);
        return value instanceof Boolean bool && bool;
    }

    private static int readInt(AbstractContainerScreen<?> screen, Field field) {
        Object value = read(screen, field);
        return value instanceof Integer integer ? integer : 0;
    }

    private static long readLong(AbstractContainerScreen<?> screen, Field field) {
        Object value = read(screen, field);
        return value instanceof Long longValue ? longValue : 0L;
    }

    private static Object read(AbstractContainerScreen<?> screen, Field field) {
        if (screen == null || field == null) {
            return null;
        }
        try {
            return field.get(screen);
        } catch (IllegalAccessException ignored) {
            return null;
        }
    }

    private static void setStack(AbstractContainerScreen<?> screen, Field field, ItemStack stack) {
        if (screen == null || field == null) {
            return;
        }
        try {
            field.set(screen, stack == null ? ItemStack.EMPTY : stack);
        } catch (IllegalAccessException ignored) {
        }
    }

    private static Field findField(String name) {
        try {
            Field field = AbstractContainerScreen.class.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException ignored) {
            return null;
        }
    }

    private record FloatingItem(
            ItemStack stack,
            String text,
            boolean dragging,
            int yOffset
    ) {
        private static FloatingItem empty() {
            return new FloatingItem(ItemStack.EMPTY, null, false, 8);
        }
    }
}
