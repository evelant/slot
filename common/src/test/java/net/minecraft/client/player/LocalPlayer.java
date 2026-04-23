package net.minecraft.client.player;

import net.minecraft.world.entity.player.Player;

/**
 * Minimal test shim — common tests don't need any real client-player
 * behaviour, just something implementing the type so interfaces that
 * mention {@code LocalPlayer} compile.
 */
public class LocalPlayer extends Player {
}
