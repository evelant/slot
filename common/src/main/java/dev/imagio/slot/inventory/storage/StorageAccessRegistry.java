package dev.imagio.slot.inventory.storage;

import java.util.Objects;

/**
 * Static registry holding the platform implementations of
 * {@link CarriedSourceAccess} and {@link WorldStorageAccess}. The platform
 * module (neoforge, fabric, forge backport) installs its implementations at
 * startup; common-module code retrieves them via the getters.
 *
 * <p>This mirrors the pattern used by {@code InventoryIntegrationRegistry} and
 * keeps the common module free of any platform-specific imports — common
 * depends only on the interfaces here, implementations are provided at
 * runtime.
 */
public final class StorageAccessRegistry {
    private static volatile CarriedSourceAccess carriedSourceAccess;
    private static volatile WorldStorageAccess worldStorageAccess;

    private StorageAccessRegistry() {
    }

    public static synchronized void installCarriedSourceAccess(CarriedSourceAccess impl) {
        carriedSourceAccess = Objects.requireNonNull(impl, "impl");
    }

    public static synchronized void installWorldStorageAccess(WorldStorageAccess impl) {
        worldStorageAccess = Objects.requireNonNull(impl, "impl");
    }

    public static CarriedSourceAccess carriedSourceAccess() {
        CarriedSourceAccess impl = carriedSourceAccess;
        if (impl == null) {
            throw new IllegalStateException(
                    "CarriedSourceAccess not installed — platform init did not run StorageAccessRegistry.installCarriedSourceAccess");
        }
        return impl;
    }

    public static WorldStorageAccess worldStorageAccess() {
        WorldStorageAccess impl = worldStorageAccess;
        if (impl == null) {
            throw new IllegalStateException(
                    "WorldStorageAccess not installed — platform init did not run StorageAccessRegistry.installWorldStorageAccess");
        }
        return impl;
    }

    /** True when both accessors are installed. Tests may skip on false. */
    public static boolean isInstalled() {
        return carriedSourceAccess != null && worldStorageAccess != null;
    }

    /** Test-only: clear installed implementations so a test can install its own. */
    public static synchronized void resetForTests() {
        carriedSourceAccess = null;
        worldStorageAccess = null;
    }
}
