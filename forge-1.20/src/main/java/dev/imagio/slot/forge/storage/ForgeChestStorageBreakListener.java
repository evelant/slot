package dev.imagio.slot.forge.storage;

import dev.imagio.slot.forge.SlotForge;
import dev.imagio.slot.forge.workflow.ForgePlayerWorkflowRuntimeService;
import dev.imagio.slot.workflow.domain.ChestAnchor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = SlotForge.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ForgeChestStorageBreakListener {
    private ForgeChestStorageBreakListener() {
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        BlockPos pos = event.getPos();
        Optional<UUID> storageId = ForgeChestStorageIds.read(level, pos);
        if (storageId.isEmpty()) {
            return;
        }
        ChestAnchor anchor = ForgeChestStorageAnchors.toAnchor(level, pos);
        Player player = event.getPlayer();
        if (player instanceof ServerPlayer serverPlayer) {
            ForgePlayerWorkflowRuntimeService.removeBrokenStorageAnchor(serverPlayer, storageId.get(), anchor);
        } else {
            ForgePlayerWorkflowRuntimeService.removeBrokenStorageAnchor(level.getServer(), storageId.get(), anchor);
        }
    }
}
