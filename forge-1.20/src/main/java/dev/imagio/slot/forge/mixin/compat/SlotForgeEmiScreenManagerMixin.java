package dev.imagio.slot.forge.mixin.compat;

import dev.imagio.slot.forge.client.ForgeSlotTextInputKeyGuard;
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
        ForgeSlotTextInputKeyGuard.Decision decision = ForgeSlotTextInputKeyGuard.decide(keyCode, modifiers);
        ForgeSlotTextInputKeyGuard.logDiagnostic(
                "EmiScreenManager.keyPressed",
                keyCode,
                scanCode,
                -1,
                modifiers,
                decision);
        if (decision.slotOwnsKey()) {
            // EMI recipe screens continue into hovered-widget shortcut checks
            // when the manager reports "unhandled"; consume the key for EMI
            // while GLFW still delivers the text char to SLOT.
            cir.setReturnValue(true);
        }
    }
}
