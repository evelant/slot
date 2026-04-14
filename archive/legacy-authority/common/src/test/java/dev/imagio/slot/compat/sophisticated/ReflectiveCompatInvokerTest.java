package dev.imagio.slot.compat.sophisticated;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ReflectiveCompatInvokerTest {
    @Test
    void invokesMethodForCompatibleReceiver() throws Exception {
        Method method = Receiver.class.getDeclaredMethod("name");
        method.setAccessible(true);

        Object result = ReflectiveCompatInvoker.invokeInstanceIfCompatible(method, Receiver.class, new Receiver());

        assertEquals("receiver", result);
    }

    @Test
    void returnsNullForIncompatibleReceiver() throws Exception {
        Method method = Receiver.class.getDeclaredMethod("name");
        method.setAccessible(true);

        Object result = ReflectiveCompatInvoker.invokeInstanceIfCompatible(method, Receiver.class, new Object());

        assertNull(result);
    }

    private static final class Receiver {
        @SuppressWarnings("unused")
        private String name() {
            return "receiver";
        }
    }
}
