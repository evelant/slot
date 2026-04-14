package dev.imagio.slot.neoforge.mixin;

import dev.imagio.slot.client.screen.container.SlotInventoryWorkspaceScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;

@Pseudo
@Mixin(targets = "com.tom.storagemod.network.NetworkHandler", remap = false)
abstract class TomsStorageNetworkHandlerMixin {
    @Inject(method = "lambda$handleDataClient$1(Lcom/tom/storagemod/network/DataPacket;)V", at = @At("TAIL"), remap = false)
    private static void slot$forwardToSlotWorkspace(@Coerce Object packet, CallbackInfo callbackInfo) {
        Screen currentScreen = Minecraft.getInstance().screen;
        if (!(currentScreen instanceof SlotInventoryWorkspaceScreen<?> workspace)) {
            return;
        }

        CompoundTag tag = readTag(packet);
        if (tag == null) {
            return;
        }

        workspace.handleTomsStorageClientPacket(tag);
    }

    private static CompoundTag readTag(Object packet) {
        if (packet == null) {
            return null;
        }
        try {
            Method tagMethod = packet.getClass().getMethod("tag");
            Object result = tagMethod.invoke(packet);
            return result instanceof CompoundTag compoundTag ? compoundTag : null;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }
}
