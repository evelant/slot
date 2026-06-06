package dev.imagio.slot.forge.mixin.compat;

import dev.imagio.slot.forge.client.ForgeSlotTextInputKeyGuard;
import net.minecraft.client.KeyboardHandler;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = KeyboardHandler.class, priority = 2000)
public abstract class SlotForgeKeyboardHandlerTextCaptureMixin {
    @Inject(method = "keyPress(JIIII)V", at = @At("HEAD"), cancellable = true)
    private void slot$letSlotTextCaptureOwnPlainKeys(
            long windowPointer,
            int key,
            int scanCode,
            int action,
            int modifiers,
            CallbackInfo info
    ) {
        if ((action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT)
                && ForgeSlotTextInputKeyGuard.shouldLetSlotOwnKey(key, modifiers)) {
            info.cancel();
        }
    }
}
