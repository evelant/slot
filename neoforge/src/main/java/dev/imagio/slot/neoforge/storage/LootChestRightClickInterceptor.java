package dev.imagio.slot.neoforge.storage;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.neoforge.screen.ldlib.SlotWorkspaceLdlibMenus;
import dev.imagio.slot.neoforge.workflow.SlotPlayerWorkflowRuntimeService;
import dev.imagio.slot.workflow.domain.ChestAnchor;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Right-click intercept for unclaimed (loot) chests: instead of letting
 * the vanilla chest GUI open, redirect the player into the SLOT atlas
 * so the loot panel surfaces the chest's contents with chip suggestions.
 *
 * <p>Only fires when the chest is genuinely unclaimed (no
 * {@code slot:storage_id} attachment, no anchor-matched workflow claim).
 * Claimed chests keep the vanilla GUI so the player can manually arrange
 * stored items. Sneaking escapes the intercept too — sneak+right-click
 * always opens the vanilla chest, mirroring vanilla's "sneak to bypass
 * SLOT-y behaviour" pattern.
 */
public final class LootChestRightClickInterceptor {
    private static boolean registered;

    private LootChestRightClickInterceptor() {
    }

    public static void init() {
        if (registered) {
            return;
        }
        NeoForge.EVENT_BUS.addListener(LootChestRightClickInterceptor::onRightClickBlock);
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
        if (ChestStorageIds.read(serverLevel, pos).isPresent()) {
            return;
        }
        ChestAnchor anchor = ChestStorageAnchors.toAnchor(serverLevel, pos);
        WorkflowDomainRuntime runtime = SlotPlayerWorkflowRuntimeService.runtime(player);
        if (runtime.chestClaimWorkflow().claimedChestMap().chestByAnchor(anchor) != null) {
            return;
        }
        // Unclaimed chest. Cancel the vanilla open and route the player
        // into the SLOT workspace; the proximity-driven loot panel
        // surfaces this chest, and the workspace's V hotkey opens the
        // chest's vanilla GUI when the panel is showing.
        event.setCanceled(true);
        SlotCommon.LOGGER.info(
                "[SLOT] loot-chest intercept player={} pos={}",
                player.getScoreboardName(), pos
        );
        SlotWorkspaceLdlibMenus.open(player);
    }
}
