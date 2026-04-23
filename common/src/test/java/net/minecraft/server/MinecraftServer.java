package net.minecraft.server;

import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Minimal test shim for {@link net.minecraft.server.MinecraftServer} — common
 * tests never need real server behaviour, just something implementing the type
 * so {@link dev.imagio.slot.inventory.storage.WorldStorageAccess} method
 * signatures compile.
 */
public class MinecraftServer {
    private final List<ServerLevel> levels = new ArrayList<>();

    public Iterable<ServerLevel> getAllLevels() {
        return Collections.unmodifiableList(levels);
    }
}
