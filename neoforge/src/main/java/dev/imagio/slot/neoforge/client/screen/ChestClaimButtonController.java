package dev.imagio.slot.neoforge.client.screen;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.neoforge.network.SlotChestClaimPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ChestClaimButtonController {
    private static final long INTERACT_WINDOW_MS = 1500L;
    private static boolean registered;
    private static BlockPos lastInteractedPos;
    private static ResourceKey<Level> lastInteractedDimension;
    private static long lastInteractedAt;

    private ChestClaimButtonController() {
    }

    public static void init() {
        if (registered) {
            return;
        }
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, ChestClaimButtonController::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(ChestClaimButtonController::onScreenInitPost);
        registered = true;
    }

    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getLevel().isClientSide()) {
            return;
        }
        lastInteractedPos = event.getPos().immutable();
        lastInteractedDimension = event.getLevel().dimension();
        lastInteractedAt = System.currentTimeMillis();
    }

    private static void onScreenInitPost(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> containerScreen)) {
            return;
        }
        if (containerScreen instanceof InventoryScreen) {
            return;
        }
        if (lastInteractedPos == null || lastInteractedDimension == null) {
            return;
        }
        if (System.currentTimeMillis() - lastInteractedAt > INTERACT_WINDOW_MS) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || !level.dimension().equals(lastInteractedDimension)) {
            return;
        }
        BlockPos pos = lastInteractedPos;
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return;
        }
        if (state.is(BlockTags.SHULKER_BOXES)) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return;
        }

        int buttonX = containerScreen.getGuiLeft() + 4;
        int buttonY = containerScreen.getGuiTop() - 16;
        int buttonWidth = 54;
        int buttonHeight = 14;
        ResourceKey<Level> dimension = lastInteractedDimension;
        Button claimButton = Button.builder(
                Component.literal("Claim"),
                btn -> {
                    PacketDistributor.sendToServer(new SlotChestClaimPayload(dimension, pos));
                    btn.active = false;
                    btn.setMessage(Component.literal("Claimed"));
                    SlotDebugLog.log("Sent SLOT claim request for {} @ {}", dimension.location(), pos);
                }
        ).bounds(buttonX, buttonY, buttonWidth, buttonHeight).build();
        event.addListener(claimButton);
    }
}
