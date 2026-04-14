package dev.imagio.slot.neoforge.mixin;

import dev.imagio.slot.neoforge.compat.emi.TomsStorageEmiRecipeHandler;
import dev.imagio.slot.neoforge.compat.emi.SophisticatedBackpackEmiRecipeHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "dev.emi.emi.registry.EmiRecipeFiller", remap = false)
abstract class EmiRecipeFillerMixin {
    @Inject(method = "getFirstValidHandler", at = @At("RETURN"), cancellable = true, remap = false)
    private static void slot$preferSlotWorkspaceHandler(@Coerce Object recipe, @Coerce Object screen, CallbackInfoReturnable<Object> cir) {
        Object preferred = SophisticatedBackpackEmiRecipeHandler.preferredHandlerForScreen(screen, recipe, cir.getReturnValue());
        preferred = TomsStorageEmiRecipeHandler.preferredHandlerForScreen(screen, recipe, preferred);
        if (preferred != cir.getReturnValue()) {
            cir.setReturnValue(preferred);
        }
    }
}
