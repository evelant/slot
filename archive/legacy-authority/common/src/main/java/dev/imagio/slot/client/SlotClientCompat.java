package dev.imagio.slot.client;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.lang.reflect.Method;

public final class SlotClientCompat {
    private static final boolean EMI_PRESENT = isClassPresent("dev.emi.emi.api.EmiPlugin");
    private static final Class<?> PACKET_DISTRIBUTOR_CLASS = loadClass("net.neoforged.neoforge.network.PacketDistributor");
    private static final Method SEND_TO_SERVER_METHOD = findSendToServerMethod();
    private static volatile boolean emiRuntimeEnabled;

    private SlotClientCompat() {
    }

    public static boolean hasEmi() {
        return EMI_PRESENT || emiRuntimeEnabled;
    }

    public static void setEmiRuntimeEnabled(boolean enabled) {
        emiRuntimeEnabled = enabled;
    }

    public static boolean sendToServer(CustomPacketPayload payload) {
        if (payload == null || SEND_TO_SERVER_METHOD == null) {
            return false;
        }

        try {
            SEND_TO_SERVER_METHOD.invoke(null, payload, new CustomPacketPayload[0]);
            return true;
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    private static boolean isClassPresent(String className) {
        return loadClass(className) != null;
    }

    private static Class<?> loadClass(String className) {
        try {
            return Class.forName(className, false, SlotClientCompat.class.getClassLoader());
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Method findSendToServerMethod() {
        if (PACKET_DISTRIBUTOR_CLASS == null) {
            return null;
        }

        try {
            return PACKET_DISTRIBUTOR_CLASS.getMethod("sendToServer", CustomPacketPayload.class, CustomPacketPayload[].class);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }
}
