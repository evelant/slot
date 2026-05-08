package dev.imagio.slot.platform;

import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

public final class SlotResourceAccess {
    private static volatile ResourceAccess current = DefaultResourceAccess.INSTANCE;

    private SlotResourceAccess() {
    }

    public static ResourceAccess current() {
        return current;
    }

    public static void install(ResourceAccess access) {
        current = Objects.requireNonNull(access, "access");
    }

    public interface ResourceAccess {
        ResourceLocation id(String namespace, String path);
    }

    // Keeps common tests and early bootstrap usable. Loader modules should
    // install a direct implementation before production registration runs.
    private enum DefaultResourceAccess implements ResourceAccess {
        INSTANCE;

        @Override
        public ResourceLocation id(String namespace, String path) {
            try {
                Method factory = ResourceLocation.class.getMethod(
                        "fromNamespaceAndPath",
                        String.class,
                        String.class
                );
                Object value = factory.invoke(null, namespace, path);
                if (value instanceof ResourceLocation location) {
                    return location;
                }
            } catch (NoSuchMethodException ignored) {
            } catch (IllegalAccessException | InvocationTargetException | RuntimeException ignored) {
            }

            try {
                Constructor<ResourceLocation> constructor = ResourceLocation.class.getConstructor(String.class, String.class);
                return constructor.newInstance(namespace, path);
            } catch (ReflectiveOperationException | RuntimeException exception) {
                throw new IllegalArgumentException(
                        "Unable to create ResourceLocation for " + namespace + ":" + path,
                        exception
                );
            }
        }
    }
}
