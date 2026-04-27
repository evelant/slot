package dev.imagio.slot.neoforge.client.screen;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.neoforge.client.SlotClientWorkspaceCache;
import dev.imagio.slot.neoforge.network.SlotChestClaimPayload;
import dev.imagio.slot.neoforge.network.SlotChestUnclaimPayload;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ChestClaimButtonController {
    private static final long INTERACT_WINDOW_MS = 1500L;
    private static final int MAX_VISIBLE_AREAS = 5;
    private static final int BUTTON_HEIGHT = 14;
    private static final int BUTTON_WIDTH = 90;
    private static final int CLAIM_WIDTH = 54;

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

        ResourceKey<Level> dimension = lastInteractedDimension;
        SlotWorkspaceViewModel.ClaimedChestTile existingTile = findClaimedTile(
                dimension.location().toString(), pos
        );
        if (existingTile != null) {
            installUnclaimButton(event, containerScreen, pos, dimension, existingTile);
        } else {
            installClaimPicker(event, containerScreen, pos, dimension);
        }
    }

    private static void installUnclaimButton(
            ScreenEvent.Init.Post event,
            AbstractContainerScreen<?> containerScreen,
            BlockPos pos,
            ResourceKey<Level> dimension,
            SlotWorkspaceViewModel.ClaimedChestTile tile
    ) {
        int claimX = containerScreen.getGuiLeft() + 4;
        int claimY = containerScreen.getGuiTop() - 16;
        String areaLabel = areaLabelFor(tile.areaId());
        String displayLabel = areaLabel == null || areaLabel.isBlank()
                ? "Unclaim"
                : "Unclaim · " + areaLabel;
        Button unclaimButton = Button.builder(
                Component.literal(displayLabel),
                btn -> {
                    PacketDistributor.sendToServer(new SlotChestUnclaimPayload(dimension, pos));
                    btn.active = false;
                    btn.setMessage(Component.literal("Unclaimed"));
                    SlotDebugLog.log("Sent SLOT unclaim for {} @ {} (was '{}')",
                            dimension.location(), pos, areaLabel);
                }
        ).bounds(claimX, claimY, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        event.addListener(unclaimButton);
    }

    private static void installClaimPicker(
            ScreenEvent.Init.Post event,
            AbstractContainerScreen<?> containerScreen,
            BlockPos pos,
            ResourceKey<Level> dimension
    ) {
        int claimX = containerScreen.getGuiLeft() + 4;
        int claimY = containerScreen.getGuiTop() - 16;

        // Popup column lands above the Claim button so it doesn't collide
        // with the chest GUI body. Stacks upward — each option is one
        // BUTTON_HEIGHT plus a 1px gutter. Capped at MAX_VISIBLE_AREAS so
        // it doesn't run off the top of the screen.
        List<AreaOption> options = buildAreaOptions(dimension.location().toString(), pos);

        PickerState pickerState = new PickerState();

        Button claimButton = Button.builder(
                Component.literal("Claim"),
                btn -> {
                    pickerState.toggle();
                }
        ).bounds(claimX, claimY, CLAIM_WIDTH, BUTTON_HEIGHT).build();

        // Build option buttons in advance, hidden until the picker opens.
        // Sorted: existing areas closest-first, then "+ New Area" at the
        // bottom of the list (visually above the others since we stack
        // upward — see optionY math).
        List<Button> optionButtons = new ArrayList<>();
        int displayedCount = Math.min(options.size(), MAX_VISIBLE_AREAS);
        for (int index = 0; index < displayedCount; index++) {
            AreaOption option = options.get(index);
            int optionY = claimY - (BUTTON_HEIGHT + 1) * (index + 1);
            Button optionButton = Button.builder(
                    Component.literal(option.label()),
                    btn -> {
                        PacketDistributor.sendToServer(new SlotChestClaimPayload(
                                dimension, pos, option.areaId(), ""
                        ));
                        SlotDebugLog.log("Sent SLOT claim into existing area '{}' for {} @ {}",
                                option.label(), dimension.location(), pos);
                        finalizeClaim(claimButton, optionButtons);
                    }
            ).bounds(claimX, optionY, BUTTON_WIDTH, BUTTON_HEIGHT).build();
            optionButton.visible = false;
            optionButtons.add(optionButton);
        }

        int newAreaIndex = displayedCount;
        int newAreaY = claimY - (BUTTON_HEIGHT + 1) * (newAreaIndex + 1);
        String autoLabel = nextAutoAreaLabel();
        Button newAreaButton = Button.builder(
                Component.literal("+ " + autoLabel),
                btn -> {
                    PacketDistributor.sendToServer(new SlotChestClaimPayload(
                            dimension, pos, "", autoLabel
                    ));
                    SlotDebugLog.log("Sent SLOT claim creating new area '{}' for {} @ {}",
                            autoLabel, dimension.location(), pos);
                    finalizeClaim(claimButton, optionButtons);
                }
        ).bounds(claimX, newAreaY, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        newAreaButton.visible = false;
        optionButtons.add(newAreaButton);

        pickerState.bind(claimButton, optionButtons);

        event.addListener(claimButton);
        for (Button btn : optionButtons) {
            event.addListener(btn);
        }
    }

    private static void finalizeClaim(Button claimButton, List<Button> optionButtons) {
        claimButton.active = false;
        claimButton.setMessage(Component.literal("Claimed"));
        for (Button btn : optionButtons) {
            btn.visible = false;
        }
    }

    private static String nextAutoAreaLabel() {
        SlotWorkspaceViewModel viewModel = SlotClientWorkspaceCache.latest();
        int existing = viewModel.storageAreas().size();
        return existing == 0 ? "Main Base" : "Area " + (existing + 1);
    }

    private static SlotWorkspaceViewModel.ClaimedChestTile findClaimedTile(
            String dimensionId,
            BlockPos pos
    ) {
        SlotWorkspaceViewModel viewModel = SlotClientWorkspaceCache.latest();
        for (SlotWorkspaceViewModel.ClaimedChestTile tile : viewModel.claimedChestTiles()) {
            if (!tile.dimensionId().equals(dimensionId)) {
                continue;
            }
            if (tile.worldX() == pos.getX()
                    && tile.worldY() == pos.getY()
                    && tile.worldZ() == pos.getZ()) {
                return tile;
            }
        }
        return null;
    }

    private static String areaLabelFor(String areaId) {
        if (areaId == null || areaId.isBlank()) {
            return null;
        }
        SlotWorkspaceViewModel.StorageAreaSnapshot snapshot =
                SlotClientWorkspaceCache.latest().storageArea(areaId);
        return snapshot == null ? null : snapshot.label();
    }

    private static List<AreaOption> buildAreaOptions(String dimensionId, BlockPos newChestPos) {
        SlotWorkspaceViewModel viewModel = SlotClientWorkspaceCache.latest();
        List<SlotWorkspaceViewModel.StorageAreaSnapshot> areas = viewModel.storageAreas();
        if (areas.isEmpty()) {
            return List.of();
        }
        List<AreaOption> options = new ArrayList<>(areas.size());
        for (SlotWorkspaceViewModel.StorageAreaSnapshot area : areas) {
            long minDistanceSq = Long.MAX_VALUE;
            for (SlotWorkspaceViewModel.ClaimedChestTile tile : area.chestTiles()) {
                if (!tile.dimensionId().equals(dimensionId)) {
                    continue;
                }
                long dx = (long) (tile.worldX() - newChestPos.getX());
                long dy = (long) (tile.worldY() - newChestPos.getY());
                long dz = (long) (tile.worldZ() - newChestPos.getZ());
                long distSq = dx * dx + dy * dy + dz * dz;
                if (distSq < minDistanceSq) {
                    minDistanceSq = distSq;
                }
            }
            String label = area.label();
            int chestCount = area.chestCount();
            String formatted = chestCount == 0
                    ? label
                    : label + " (" + chestCount + ")";
            options.add(new AreaOption(area.areaId(), formatted, minDistanceSq));
        }
        options.sort(Comparator.comparingLong(AreaOption::distanceSq));
        return options;
    }

    private record AreaOption(String areaId, String label, long distanceSq) {
    }

    private static final class PickerState {
        private boolean open;
        private Button claimButton;
        private List<Button> optionButtons = List.of();

        void bind(Button claim, List<Button> options) {
            this.claimButton = claim;
            this.optionButtons = options;
        }

        void toggle() {
            if (claimButton == null) {
                return;
            }
            open = !open;
            for (Button btn : optionButtons) {
                btn.visible = open;
            }
            claimButton.setMessage(Component.literal(open ? "Cancel" : "Claim"));
        }
    }
}
