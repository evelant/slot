package dev.imagio.slot.neoforge.storage;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.neoforge.screen.ldlib.SlotWorkspaceLdlibMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Right-click intercept for chests: instead of letting the vanilla chest
 * GUI open, redirect the player into the SLOT atlas. Fires for both
 * tracked (claimed) and untracked (loot) chests — the workspace surfaces
 * tracked chests through the storage panel and untracked chests through
 * the proximity loot panel.
 *
 * <p>Sneak escapes the intercept: sneak+right-click always opens the
 * vanilla chest GUI, mirroring vanilla's "sneak to bypass SLOT-y
 * behaviour" pattern. The workspace's V hotkey also opens the focused
 * chest's vanilla GUI from inside SLOT.
 */
public final class ChestRightClickInterceptor {
    private static boolean registered;

    private ChestRightClickInterceptor() {
    }

    public static void init() {
        if (registered) {
            return;
        }
        NeoForge.EVENT_BUS.addListener(ChestRightClickInterceptor::onRightClickBlock);
        registered = true;
    }

    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (player.isShiftKeyDown()) {
            return;
        }
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!ChestStorageAnchors.isClaimable(serverLevel, pos)) {
            return;
        }
        event.setCanceled(true);
        SlotCommon.LOGGER.info(
                "[SLOT] chest intercept player={} pos={}",
                player.getScoreboardName(), pos
        );
        SlotWorkspaceLdlibMenus.open(player);
    }
}
