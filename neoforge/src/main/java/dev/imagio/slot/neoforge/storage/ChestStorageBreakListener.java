package dev.imagio.slot.neoforge.storage;

import dev.imagio.slot.neoforge.workflow.SlotPlayerWorkflowRuntimeService;
import dev.imagio.slot.workflow.domain.ChestAnchor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.Optional;
import java.util.UUID;

public final class ChestStorageBreakListener {
    private static boolean registered;

    private ChestStorageBreakListener() {
    }

    public static void init() {
        if (registered) {
            return;
        }
        NeoForge.EVENT_BUS.addListener(ChestStorageBreakListener::onBlockBreak);
        registered = true;
    }

    private static void onBlockBreak(BlockEvent.BreakEvent event) {
        LevelAccessor accessor = event.getLevel();
        if (!(accessor instanceof ServerLevel level)) {
            return;
        }
        BlockPos pos = event.getPos();
        Optional<UUID> storageId = ChestStorageIds.read(level, pos);
        if (storageId.isEmpty()) {
            return;
        }
        Player player = event.getPlayer();
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ChestAnchor anchor = ChestStorageAnchors.toAnchor((Level) level, pos);
        SlotPlayerWorkflowRuntimeService.runtime(serverPlayer)
                .chestClaimWorkflow()
                .removeAnchor(storageId.get(), anchor);
    }
}
