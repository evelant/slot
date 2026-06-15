package dev.imagio.slot.neoforge.mixin.compat;

import dev.imagio.slot.neoforge.screen.ldlib.SlotWorkspaceTextInputKeyGuard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "dev.emi.emi.screen.EmiScreenManager", remap = false)
public abstract class SlotEmiScreenManagerMixin {
    @Inject(method = "keyPressed(III)Z", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void slot$letSlotTextCaptureOwnPlainKeys(
            int keyCode,
            int scanCode,
            int modifiers,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (SlotWorkspaceTextInputKeyGuard.shouldLetSlotOwnKey(keyCode, modifiers)) {
            // EMI recipe screens continue into hovered-widget shortcut checks
            // when the manager reports "unhandled"; consume the key for EMI
            // while GLFW still delivers the text char to SLOT.
            cir.setReturnValue(true);
        }
    }
}
