package dev.imagio.slot.forge.mixin.compat;

import dev.imagio.slot.forge.client.ForgeSlotTextInputCapture;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "dev.emi.emi.screen.EmiScreenManager", remap = false)
public abstract class SlotForgeEmiScreenManagerMixin {
    @Inject(method = "keyPressed(III)Z", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void slot$letSlotTextCaptureOwnPlainKeys(
            int keyCode,
            int scanCode,
            int modifiers,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (ForgeSlotTextInputCapture.isActive() && isPlainTextKey(keyCode, modifiers)) {
            cir.setReturnValue(false);
        }
    }

    private static boolean isPlainTextKey(int keyCode, int modifiers) {
        int passthroughModifiers = GLFW.GLFW_MOD_CONTROL | GLFW.GLFW_MOD_ALT | GLFW.GLFW_MOD_SUPER;
        if ((modifiers & passthroughModifiers) != 0) {
            return false;
        }
        return keyCode >= GLFW.GLFW_KEY_SPACE && keyCode <= GLFW.GLFW_KEY_WORLD_2;
    }
}
