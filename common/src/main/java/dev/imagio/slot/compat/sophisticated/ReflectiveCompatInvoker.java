package dev.imagio.slot.compat.sophisticated;

import java.lang.reflect.Method;

public final class ReflectiveCompatInvoker {
    private ReflectiveCompatInvoker() {
    }

    public static Object invokeInstanceIfCompatible(Method method, Class<?> receiverClass, Object receiver, Object... args) {
        if (method == null || receiverClass == null || receiver == null || !receiverClass.isInstance(receiver)) {
            return null;
        }
        try {
            return method.invoke(receiver, args);
        } catch (ReflectiveOperationException | IllegalArgumentException ignored) {
            return null;
        }
    }
}
