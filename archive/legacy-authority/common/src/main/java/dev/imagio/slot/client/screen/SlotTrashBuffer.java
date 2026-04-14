package dev.imagio.slot.client.screen;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class SlotTrashBuffer {
    private static ItemStack lastDestroyedStack = ItemStack.EMPTY;
    private static ItemStack lastAutoVoidedStack = ItemStack.EMPTY;

    private SlotTrashBuffer() {
    }

    public static ItemStack previewStack() {
        return lastDestroyedStack.isEmpty() ? ItemStack.EMPTY : lastDestroyedStack.copy();
    }

    public static ItemStack lastAutoVoidedPreviewStack() {
        return lastAutoVoidedStack.isEmpty() ? ItemStack.EMPTY : lastAutoVoidedStack.copy();
    }

    public static void remember(ItemStack trashedStack) {
        rememberManualTrash(trashedStack);
    }

    public static void rememberManualTrash(ItemStack trashedStack) {
        if (trashedStack == null || trashedStack.isEmpty()) {
            return;
        }
        lastDestroyedStack = trashedStack.copy();
    }

    public static void rememberAutoVoid(ItemStack trashedStack) {
        if (trashedStack == null || trashedStack.isEmpty()) {
            return;
        }
        lastDestroyedStack = trashedStack.copy();
        lastAutoVoidedStack = trashedStack.copy();
    }

    public static List<Component> buildTooltipLines(SlotTrashWarningState warningState) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("slot.screen.inventory.trash"));

        if (warningState != null && warningState.pausedForExternalStorage()) {
            lines.add(Component.translatable("slot.screen.inventory.trash.auto_void_paused"));
        }
        if (warningState != null && warningState.active()) {
            lines.add(Component.translatable(
                    "slot.screen.inventory.trash.auto_void_next",
                    warningState.nextStack().getHoverName()
            ));
        }

        ItemStack lastAutoVoided = lastAutoVoidedPreviewStack();
        if (!lastAutoVoided.isEmpty()) {
            lines.add(Component.translatable(
                    "slot.screen.inventory.trash.auto_void_last",
                    lastAutoVoided.getHoverName(),
                    lastAutoVoided.getCount()
            ));
        }

        ItemStack previewStack = previewStack();
        if (!previewStack.isEmpty() && !samePreview(lastAutoVoided, previewStack)) {
            lines.add(Component.translatable(
                    "slot.screen.inventory.trash.preview",
                    previewStack.getHoverName(),
                    previewStack.getCount()
            ));
        }

        lines.add(Component.translatable("slot.screen.inventory.trash.hint"));
        return List.copyOf(lines);
    }

    private static boolean samePreview(ItemStack first, ItemStack second) {
        return !first.isEmpty()
                && !second.isEmpty()
                && first.getCount() == second.getCount()
                && ItemStack.isSameItemSameComponents(first, second);
    }
}
